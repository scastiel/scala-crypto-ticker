package me.castiel.ticker

import rx.lang.scala.Observable

import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by sebastien on 09/05/2017.
  */
object TickerObserver {

  def observableForTickerAPI(tickerAPI: TickerAPI, tickers: List[Ticker], interval: Duration): Observable[List[TickerValue]] =
    Observable.interval(interval)
      .flatMap(_ => Observable.from(tickerAPI.getTickersValues(tickers)))

  def observableForTickerAPIs(tickerAPIs: Map[String, TickerAPI], tickers: List[Ticker], interval: Duration): Observable[(String, List[TickerValue])] =
    tickerAPIs
      .map({ case (source, tickerAPI) =>
        observableForTickerAPI(tickerAPI, tickers, interval).map((source, _))
      })
      .reduceLeft((acc, o) => acc.merge(o))

}
