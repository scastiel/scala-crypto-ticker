package me.castiel.ticker

import play.api.libs.json.{JsArray, JsValue}
import play.api.libs.ws.WSClient
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
  * Created by sebastien on 07/05/2017.
  */
class KrakenAPI(client: WSClient) extends TickerAPI {

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
    client.url(url).get().map(response => {
      (response.json \ "error").get match {
        case JsArray(err +: _) => throw new TickerApiException("Kraken API error:" + err.toString())
        case _ => (response.json \ "result").get
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
