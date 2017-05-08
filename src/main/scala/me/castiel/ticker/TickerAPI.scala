package me.castiel.ticker

import scala.concurrent.Future

/**
  * Created by sebastien on 07/05/2017.
  */
abstract class TickerAPI {
  type MaybeTickerValue = Future[TickerValue]
  type MaybeTickersValues = Future[List[TickerValue]]
  def getTickerValue(ticker: Ticker): MaybeTickerValue
  def getTickersValues(tickers: List[Ticker]): MaybeTickersValues
}
