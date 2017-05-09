package me.castiel.ticker

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

  TickerObserver.observableForTickerAPIs(tickerAPIs, tickers, 5 seconds)
    .subscribe(tickerValues => println(tickerValues))

}
