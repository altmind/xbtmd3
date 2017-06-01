package cwr.exchanges;

import cwr.DataSaver;
import info.bitrich.xchangestream.bitstamp.BitstampStreamingExchange;
import org.knowm.xchange.currency.CurrencyPair;

import java.util.logging.Logger;

public class BitstampExchange extends Exchange implements Runnable {



  public BitstampExchange(DataSaver ds, CurrencyPair... currencyPairs) {
    super(ds, currencyPairs);
    exchangeName = BitstampStreamingExchange.class;
    LOGGER = Logger.getLogger(BitstampExchange.class.getName());
  }


}
