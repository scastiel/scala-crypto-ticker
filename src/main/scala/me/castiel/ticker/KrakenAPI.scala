package me.castiel.ticker

import play.api.libs.json.{JsArray, JsValue, Json}

import scala.io.Source

/**
  * Created by sebastien on 07/05/2017.
  */
class KrakenAPI extends TickerAPI {

  private val krakenCurrenciesSymbols: Map[String, String] = Map(
    "BTC" -> "XXBT",
    "ETH" -> "XETH",
    "USD" -> "ZUSD"
  )

  private def krakenTickerSymbol(ticker: Ticker): String =
    krakenCurrenciesSymbols(ticker.from.symbol) + krakenCurrenciesSymbols(ticker.to.symbol)

  private def krakenUrlForTickers(tickers: List[Ticker]): String =
    "https://api.kraken.com/0/public/Ticker?pair=" + tickers.map(krakenTickerSymbol).mkString(",")

  private def callApi(url: String): Either[Error, JsValue] = {
    val json = Json.parse(Source.fromURL(url).mkString)
    json \ "error" match {
      case JsArray(err +: _) => Left(new Error("Kraken API error:" + err.toString()))
      case _ => Right(json \ "result")
    }
  }

  override def getTickersValues(tickers: List[Ticker]): MaybeTickersValues = {
    callApi(krakenUrlForTickers(tickers)) match {
      case Left(error: Error) => Left(error)
      case Right(result: JsValue) =>
        Right(tickers.map(ticker => {
          val value = (result \ krakenTickerSymbol(ticker) \ "c") (0).as[String].toDouble
          new TickerValue(ticker, value)
        }))
    }
  }

  override def getTickerValue(ticker: Ticker): MaybeTickerValue =
    getTickersValues(List(ticker)) match {
      case Left(error: Error) => Left(error)
      case Right(List(tickerValue)) => Right(tickerValue)
    }
}
