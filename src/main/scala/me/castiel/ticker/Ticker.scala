package me.castiel.ticker

import me.castiel.ticker

/**
  * Created by sebastien on 07/05/2017.
  */
class Ticker(val from: ticker.Currency, val to: ticker.Currency) {
  override val toString: String = from + " -> " + to
}
