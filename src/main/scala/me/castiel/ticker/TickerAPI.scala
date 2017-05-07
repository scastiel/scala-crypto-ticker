package me.castiel.ticker

/**
  * Created by sebastien on 07/05/2017.
  */
abstract class TickerAPI {
  type MaybeTickerValue = Either[Error, TickerValue]
  type MaybeTickersValues = Either[Error, List[TickerValue]]
  def getTickerValue(ticker: Ticker): MaybeTickerValue
  def getTickersValues(tickers: List[Ticker]): MaybeTickersValues
}
