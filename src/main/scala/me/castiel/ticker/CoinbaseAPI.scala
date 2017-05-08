package me.castiel.ticker

import play.api.libs.json._
import dispatch.Defaults._

import scala.concurrent.Future

/**
  * Created by sebastien on 07/05/2017.
  */
class CoinbaseAPI(http: dispatch.Http) extends TickerAPI {

  def callApi(url: String): Future[JsValue] = {
    val svc = dispatch.url(url)
    val response: dispatch.Future[String] = http(svc.OK(dispatch.as.String))
    response.map(content => {
      val json = Json.parse(content)
      json \ "errors" match {
        case JsDefined(JsArray(err +: _)) => throw new Error("Coinbase API error:" + (err(0) \ "message").as[String])
        case JsUndefined() => (json \ "data").get
      }
    })
  }

  def getEthBtcValue: Future[Double] = {
    val ethUsdValue: Future[Double] = getValueForUrl(getUrl("ETH", "USD"))
    val btcUsdValue: Future[Double] = getValueForUrl(getUrl("BTC", "USD"))
    val ethUsdBtcUsdValues: Future[(Double, Double)] = ethUsdValue.flatMap(ethUsd => btcUsdValue.map(btcUsd => (ethUsd, btcUsd)))
    ethUsdBtcUsdValues.map(values => values._1 / values._2)
  }

  def getUrl(from: String, to: String): String =
    "https://api.coinbase.com/v2/prices/" + from + "-" + to + "/spot"

  def getValueForUrl(url: String): Future[Double] =
    callApi(url).map(data => (data \ "amount").as[String].toDouble)

  def getTickerValue(ticker: Ticker): MaybeTickerValue = {
    val value =
      if (ticker.from.symbol == "ETH" && ticker.to.symbol == "BTC") getEthBtcValue
      else getValueForUrl(getUrl(ticker.from.symbol, ticker.to.symbol))
    value.map(new TickerValue(ticker, _))
  }

  override def getTickersValues(tickers: List[Ticker]): MaybeTickersValues = {
    Future.sequence(tickers.map(getTickerValue))
  }

}
