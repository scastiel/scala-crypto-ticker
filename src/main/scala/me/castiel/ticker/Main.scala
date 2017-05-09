package me.castiel.ticker

import java.math.MathContext

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.duration._

/**
  * Created by sebastien on 07/05/2017.
  */
object Main extends App {

  val currencies: Map[String, Currency] = Map(
    "ETH" -> new Currency("ether", "ETH"),
    "BTC" -> new Currency("bitcoin", "BTC"),
    "USD" -> new Currency("US dollar", "USD")
  )

  val tickers: List[Ticker] = List(
    new Ticker(currencies("BTC"), currencies("USD")),
    new Ticker(currencies("ETH"), currencies("USD")),
    new Ticker(currencies("ETH"), currencies("BTC"))
  )

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  val wsClient = AhcWSClient()

  val tickerAPIs: Map[String, TickerAPI] = Map(
    "kraken" -> new KrakenAPI(wsClient),
    "coinbase" -> new CoinbaseAPI(wsClient)
  )

  def printTickerValue(source: String, tickerValues: List[TickerValue]): Unit =
    println(
      "%s:\n%s".format(
        source.capitalize,
        tickerValues.map(tickerValue =>
          f"• 1 ${tickerValue.ticker.from.symbol} = ${BigDecimal(tickerValue.value).round(new MathContext(3)).toDouble}%6s ${tickerValue.ticker.to.symbol}"
        ).mkString("\n"))
    )

  val observable = TickerObserver.observableForTickerAPIs(tickerAPIs, tickers, 5 seconds)
    .subscribe(pair => pair match {
      case (source: String, tickerValues: List[TickerValue]) => printTickerValue(source, tickerValues)
    })

}
