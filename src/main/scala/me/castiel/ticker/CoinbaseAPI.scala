package me.castiel.ticker

import play.api.libs.json._

import scala.io.Source
import scala.util.{Failure, Success, Try}

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

  def getEthBtcValue: Try[Double] = {
    val ethUsdValue = getValueForUrl(getUrl("ETH", "USD"))
    val btcUsdValue = getValueForUrl(getUrl("BTC", "USD"))
    (ethUsdValue, btcUsdValue) match {
      case (Failure(error), _) => Failure(error)
      case (_, Failure(error)) => Failure(error)
      case (Success(ethUsd: Double), Success(btcUsd: Double)) => Success(ethUsd / btcUsd)
    }
  }

  def getUrl(from: String, to: String): String =
    "https://api.coinbase.com/v2/prices/" + from + "-" + to + "/spot"

  def getValueForUrl(url: String): Try[Double] = {
    callApi(url) match {
      case Left(error: Error) => Failure(error)
      case Right(data: JsValue) => Success((data \ "amount").as[String].toDouble)
    }
  }

  def getTickerValue(ticker: Ticker): MaybeTickerValue = {
    val value =
      if (ticker.from.symbol == "ETH" && ticker.to.symbol == "BTC") getEthBtcValue
      else getValueForUrl(getUrl(ticker.from.symbol, ticker.to.symbol))
    value match {
      case Failure(error) => Failure(error)
      case Success(value: Double) => Success(new TickerValue(ticker, value))
    }
  }

  override def getTickersValues(tickers: List[Ticker]): MaybeTickersValues = {
    def addTickerValue(tickersValues: MaybeTickersValues, ticker: Ticker): MaybeTickersValues =
      tickersValues match {
        case Failure(_) => tickersValues
        case Success(tickers: List[TickerValue]) =>
          getTickerValue(ticker) match {
            case Failure(error) => Failure(error)
            case Success(tickerValue) => Success(tickers ++ List(tickerValue))
          }
      }
    tickers.foldLeft[MaybeTickersValues](Success(List()))(addTickerValue)
  }

}
