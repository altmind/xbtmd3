package cwr.exchanges;

import cwr.DataSaver;
import org.knowm.xchange.currency.CurrencyPair;

import java.util.logging.Logger;

/**
 * Created by ahurynovich on 6/1/17.
 */
public abstract class MarketSource implements Runnable {
  static Logger LOGGER = Logger.getLogger(Exchange.class.getName());

  Object lock = new Object();
  DataSaver ds;
  CurrencyPair[] currencyPairs;
  String[] currencies;
  public boolean stopped = false;

  public MarketSource(DataSaver ds, CurrencyPair[] currencyPairs) {
    this.ds = ds;
    this.currencyPairs = currencyPairs;
  }

  public MarketSource(DataSaver ds, String[] currencies) {
    this.ds = ds;
    this.currencies = currencies;
  }

  public void setStopped(boolean stopped) {
    this.stopped = stopped;
    synchronized (lock) {
      lock.notifyAll();
    }
  }

}
