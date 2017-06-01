package cwr;

import cwr.exchanges.*;
import org.knowm.xchange.currency.CurrencyPair;

class XbtMdMain {

  public static void main(String[] args) {

    DataSaver ds = new DataSaver();

    //new CurrencyPair[]{CurrencyPair.BTC_USD, CurrencyPair.BTC_EUR, CurrencyPair.USD_XRP}
    BitstampExchange bitstampExchange = new BitstampExchange(ds, new CurrencyPair[]{CurrencyPair.BTC_USD, CurrencyPair.BTC_EUR, CurrencyPair.USD_XRP});
    Thread bitstampThread = new Thread(bitstampExchange);
    //bitstampThread.start();

    // new CurrencyPair[]{CurrencyPair.BTC_USD, CurrencyPair.BTC_EUR, CurrencyPair.ETH_USD, CurrencyPair.LTC_USD}
    GdaxExchange gdaxExchange = new GdaxExchange(ds, new CurrencyPair[]{CurrencyPair.BTC_USD, CurrencyPair.BTC_EUR, CurrencyPair.ETH_USD, CurrencyPair.LTC_USD});
    Thread gdaxThread = new Thread(gdaxExchange);
    //gdaxThread.start();

    PoloniexExchange poloniexExchange = new PoloniexExchange(ds, new CurrencyPair[]{new CurrencyPair("BTC", "ETH")});
    Thread poloniexThread = new Thread(poloniexExchange);
    //poloniexThread.start();

    CoinMarketCap cmcMarket = new CoinMarketCap(ds, new String[]{CoinMarketCapCurrencies.BITCOIN.slug, CoinMarketCapCurrencies.ETHERIUM.slug, CoinMarketCapCurrencies.AUGUR.slug, CoinMarketCapCurrencies.LITECOIN.slug, CoinMarketCapCurrencies.MONERO.slug});
    Thread cmcThread = new Thread(cmcMarket);
    cmcThread.start();

    Runnable shutdownAction = new Runnable() {
      public void run() {
        poloniexExchange.setStopped(true);
        gdaxExchange.setStopped(true);
        bitstampExchange.setStopped(true);
        cmcMarket.setStopped(true);
      }
    };
    Runtime.getRuntime().addShutdownHook(new Thread(shutdownAction));

    try {
      Thread.sleep(Long.MAX_VALUE);
    } catch (InterruptedException e) {
      shutdownAction.run();
    }
  }


}
