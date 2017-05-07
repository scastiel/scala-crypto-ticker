package me.castiel.ticker

import play.api.libs.json._

import scala.io.Source

/**
  * Created by sebastien on 07/05/2017.
  */
class CoinbaseAPI extends TickerAPI {

  def callApi(url: String): Either[Error, JsValue] = {
    val json = Json.parse(Source.fromURL(url).mkString)
    json \ "errors" match {
      case JsDefined(JsArray(err +: _)) => Left(new Error("Coinbase API error:" + (err(0) \ "message").as[String]))
      case JsUndefined() => Right((json \ "data").get)
    }
  }

  def getEthBtcValue: Either[Error, Double] = {
    val ethUsdValue = getValueForUrl(getUrl("ETH", "USD"))
    val btcUsdValue = getValueForUrl(getUrl("BTC", "USD"))
    (ethUsdValue, btcUsdValue) match {
      case (Left(error: Error), _) => Left(error)
      case (_, Left(error: Error)) => Left(error)
      case (Right(ethUsd: Double), Right(btcUsd: Double)) => Right(ethUsd / btcUsd)
    }
  }

  def getUrl(from: String, to: String): String =
    "https://api.coinbase.com/v2/prices/" + from + "-" + to + "/spot"

  def getValueForUrl(url: String): Either[Error, Double] = {
    callApi(url) match {
      case Left(error: Error) => Left(error)
      case Right(data: JsValue) => Right((data \ "amount").as[String].toDouble)
    }
  }

  def getTickerValue(ticker: Ticker): MaybeTickerValue = {
    val value =
      if (ticker.from.symbol == "ETH" && ticker.to.symbol == "BTC") getEthBtcValue
      else getValueForUrl(getUrl(ticker.from.symbol, ticker.to.symbol))
    value match {
      case Left(error: Error) => Left(error)
      case Right(value: Double) => Right(new TickerValue(ticker, value))
    }
  }

  override def getTickersValues(tickers: List[Ticker]): MaybeTickersValues = {
    def addTickerValue(tickersValues: Either[Error, List[TickerValue]], ticker: Ticker): MaybeTickersValues =
      tickersValues match {
        case Left(_) => tickersValues
        case Right(tickers: List[TickerValue]) =>
          getTickerValue(ticker) match {
            case Left(error) => Left(error)
            case Right(tickerValue) => Right(tickers ++ List(tickerValue))
          }
      }
    tickers.foldLeft[MaybeTickersValues](Right(List()))(addTickerValue)
  }

}
