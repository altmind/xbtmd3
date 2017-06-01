package cwr.exchanges;

import cwr.DataSaver;
import info.bitrich.xchangestream.bitstamp.BitstampStreamingExchange;
import info.bitrich.xchangestream.poloniex.PoloniexStreamingExchange;
import org.knowm.xchange.currency.CurrencyPair;

import java.util.logging.Logger;

public class PoloniexExchange extends Exchange implements Runnable {


  public PoloniexExchange(DataSaver ds, CurrencyPair... currencyPairs) {
    super(ds, currencyPairs);
    CONNECT_TIMEOUT_SEC = 60; // poloniex is SLOW
    DATA_TIMEOUT_SEC = 30;
    exchangeName = PoloniexStreamingExchange.class;
    LOGGER = Logger.getLogger(PoloniexExchange.class.getName());
  }


}
