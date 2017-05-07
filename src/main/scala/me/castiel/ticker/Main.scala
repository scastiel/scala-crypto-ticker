package me.castiel.ticker

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

  val tickerAPIs: Map[String, TickerAPI] = Map(
    "kraken" -> new KrakenAPI,
    "coinbase" -> new CoinbaseAPI
  )

  println(tickerAPIs.mapValues(_.getTickersValues(tickers)))

}
