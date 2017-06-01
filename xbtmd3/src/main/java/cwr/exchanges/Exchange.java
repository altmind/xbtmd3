package cwr.exchanges;

import com.sun.org.apache.xpath.internal.operations.Bool;
import cwr.DataSaver;
import info.bitrich.xchangestream.bitstamp.BitstampStreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import io.reactivex.disposables.Disposable;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.exceptions.NotYetImplementedForExchangeException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Exchange extends MarketSource implements Runnable {


  List<Disposable> disposables = new ArrayList<Disposable>(50);
  Class<? extends StreamingExchange> exchangeName;

  int CONNECT_TIMEOUT_SEC = 15;
  int DATA_TIMEOUT_SEC = 60;

  public Exchange(DataSaver ds, CurrencyPair... currencyPairs) {
    super(ds, currencyPairs);
  }


  public void run() {
    while (!stopped) {

      StreamingExchange exchange = null;
      try {
        exchange = StreamingExchangeFactory.INSTANCE.createExchange(exchangeName.getName());

        exchange.connect().timeout(CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS).blockingAwait();

        for (CurrencyPair c : currencyPairs) {
          try {
            disposables.add(exchange.getStreamingMarketDataService()
                .getTrades(c)/*.timeout(DATA_TIMEOUT_SEC, TimeUnit.SECONDS)*/
                .subscribe(trade -> {
                  ds.saveTrade(this, trade);
                }, throwable -> {
                  LOGGER.log(java.util.logging.Level.SEVERE, "{0} {1}", new Object[]{this.exchangeName.getName(), throwable});
                  synchronized (lock) {
                    lock.notifyAll();
                  }
                }));
          } catch (NotAvailableFromExchangeException | NotYetImplementedForExchangeException notAvailableFromExchangeException) {
            LOGGER.log(Level.FINER, "notAvailableFromExchangeException from {0}, {1}: {2}", new Object[]{this.exchangeName.getName(), c, notAvailableFromExchangeException});
          }
          try {
            disposables.add(exchange.getStreamingMarketDataService()
                .getOrderBook(c)/*.timeout(DATA_TIMEOUT_SEC, TimeUnit.SECONDS)*/
                .subscribe(orderBook -> {
                  ds.saveOrderBook(this, orderBook);
                }, throwable -> {
                  LOGGER.log(java.util.logging.Level.SEVERE, "{0} {1}", new Object[]{this.exchangeName.getName(), throwable});
                  synchronized (lock) {
                    lock.notifyAll();
                  }
                }));
          } catch (NotAvailableFromExchangeException | NotYetImplementedForExchangeException notAvailableFromExchangeException) {
            LOGGER.log(Level.FINER, "notAvailableFromExchangeException from {0}, {1}: {2}", new Object[]{this.exchangeName.getName(), c, notAvailableFromExchangeException});
          }
          try {
            disposables.add(exchange.getStreamingMarketDataService()
                .getTicker(new CurrencyPair(c.counter, c.base)) /*.timeout(DATA_TIMEOUT_SEC, TimeUnit.SECONDS)*/
                .subscribe(ticker -> {
                  ds.saveTicker(this, ticker);
                }, throwable -> {
                  LOGGER.log(java.util.logging.Level.SEVERE, "{0} {1}", new Object[]{this.exchangeName.getName(), throwable});
                  synchronized (lock) {
                    lock.notifyAll();
                  }
                }));
          } catch (NotAvailableFromExchangeException | NotYetImplementedForExchangeException notAvailableFromExchangeException) {
            LOGGER.log(Level.FINER, "notAvailableFromExchangeException from {0}, {1}: {2}", new Object[]{this.exchangeName.getName(), c, notAvailableFromExchangeException});
          }

        }
        try {
          synchronized (lock) {
            lock.wait();
          }
        } catch (InterruptedException e) {
        }

      } catch (Exception e) {
        e.printStackTrace();
        LOGGER.log(java.util.logging.Level.SEVERE, "{0} {1}", new Object[]{this.exchangeName.getName(), e});
      } finally {
        if (disposables != null) {
          for (Disposable d : disposables) {
            d.dispose();
          }
          disposables.clear();
        }
        if (exchange != null) {
          exchange.disconnect();
        }
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
      }
    }
  }
}
