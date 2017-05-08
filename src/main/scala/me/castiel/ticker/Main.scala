package me.castiel.ticker

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

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
    "kraken" -> new KrakenAPI(dispatch.Http),
    "coinbase" -> new CoinbaseAPI(dispatch.Http)
  )

  def listOfFuturesToFutureOfList[T](listOfFutures: Iterable[Future[T]]): Future[Iterable[T]] =
    listOfFutures.foldLeft(Future.successful(List[T]()))(
      (futureOfList: Future[List[T]], future: Future[T]) =>
        futureOfList.flatMap(values => future.map(value => values ++ List(value)))
    )

  val tickersValuesFuture = Future.sequence(tickerAPIs.toList.map({
    case (name, tickerApi) => tickerApi.getTickersValues(tickers).map((name, _))
  }))

  tickersValuesFuture onComplete {
    case Success(tickersValues) =>
      println(tickersValues)
      dispatch.Http.shutdown()
    case Failure(error) =>
      println("Error: " + error)
      dispatch.Http.shutdown()
  }

}
