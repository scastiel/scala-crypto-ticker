package me.castiel.ticker

import play.api.libs.json.{JsArray, JsValue, Json}
import dispatch.Defaults._
import scala.concurrent.Future


/**
  * Created by sebastien on 07/05/2017.
  */
class KrakenAPI(http: dispatch.Http) extends TickerAPI {

  private val krakenCurrenciesSymbols: Map[String, String] = Map(
    "BTC" -> "XXBT",
    "ETH" -> "XETH",
    "USD" -> "ZUSD"
  )

  private def krakenTickerSymbol(ticker: Ticker): String =
    krakenCurrenciesSymbols(ticker.from.symbol) + krakenCurrenciesSymbols(ticker.to.symbol)

  private def krakenUrlForTickers(tickers: List[Ticker]): String =
    "https://api.kraken.com/0/public/Ticker?pair=" + tickers.map(krakenTickerSymbol).mkString(",")

  private def callApi(url: String): Future[JsValue] = {
    val svc = dispatch.url(url)
    val response: dispatch.Future[String] = http(svc.OK(dispatch.as.String))
    response.map(content => {
      val json = Json.parse(content)
      (json \ "error").get match {
        case JsArray(err +: _) => throw new Error("Kraken API error:" + err.toString())
        case _ => (json \ "result").get
      }
    })
  }

  override def getTickersValues(tickers: List[Ticker]): MaybeTickersValues =
    callApi(krakenUrlForTickers(tickers)) map {
      (result: JsValue) =>
        tickers.map(ticker => {
          val value = (result \ krakenTickerSymbol(ticker) \ "c") (0).as[String].toDouble
          new TickerValue(ticker, value)
        })
    }

  override def getTickerValue(ticker: Ticker): MaybeTickerValue =
    getTickersValues(List(ticker)).map(_.head)
}
