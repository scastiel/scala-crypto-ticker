package me.castiel.ticker

/**
  * Created by sebastien on 07/05/2017.
  */
class TickerValue(val ticker: Ticker, val value: Double) {
  override val toString: String = ticker + " = " + value
}
