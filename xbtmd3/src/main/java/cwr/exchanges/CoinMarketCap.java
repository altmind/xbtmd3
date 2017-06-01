package cwr.exchanges;


import cwr.DataSaver;
import info.bitrich.xchangestream.gdax.GDAXStreamingExchange;
import jodd.jerry.Jerry;
import org.apache.commons.io.IOUtils;
import org.knowm.xchange.currency.CurrencyPair;

import static jodd.jerry.Jerry.jerry;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.logging.Logger;


public class CoinMarketCap extends MarketSource {


  Charset UTF8_CHARSET = Charset.forName("UTF-8");

  BufferedInputStream bis = null;
  int WAIT_BETWEEN_UPDATES = 30000;

  public CoinMarketCap(DataSaver ds, String... currencies) {
    super(ds, currencies);
    LOGGER = Logger.getLogger(CoinMarketCap.class.getName());
  }

  public static Double tryParseDouble(String s) {
    try {
      return Double.parseDouble(s.trim());
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public void run() {
    while (!stopped) {

      for (String c : currencies) {
        try {
          URL url = new URL("https://coinmarketcap.com/currencies/" + c + "/");
          LOGGER.info("Getting " + url.toString());
          URLConnection con = url.openConnection();
          con.setConnectTimeout(5000);
          con.setReadTimeout(5000);
          bis = new BufferedInputStream(con.getInputStream());
          Jerry doc = jerry(IOUtils.toString(bis, UTF8_CHARSET));
          doc.find("#markets-table tbody tr").forEach(tr -> {
            String exchange = tr.find("td:eq(1) a").text();
            String pair = tr.find("td:eq(2) a").text();
            String volPercent = tr.find("td:eq(5)").text();
            String updated = tr.find("td:eq(6)").text();
            Jerry tr_v = tr.find(".volume");
            Jerry tr_p = tr.find(".price");
            Double volume_usd = tryParseDouble(tr_v.attr("data-usd"));
            Double volume_btc = tryParseDouble(tr_v.attr("data-btc"));
            String volume_native = tr_v.attr("data-native").replace(",", "");
            Double price_usd = tryParseDouble(tr_p.attr("data-usd"));
            Double price_btc = tryParseDouble(tr_p.attr("data-btc"));
            String price_native = tr_p.attr("data-native").replace(",", "");
            boolean no_fees = tr_v.text().contains("**");
            boolean save_data = volume_usd != null && volume_usd > 100000 && updated.contains("Recently");
            if (save_data) {
              CoinMarketCapRecord r = new CoinMarketCapRecord(exchange, pair, volume_usd, volume_btc, volume_native, price_usd, price_btc, price_native, no_fees);
              ds.saveCoinMarketCapPrice(c, r);
            }
          });
        } catch (Exception e) {
          LOGGER.warning(e.toString());
        } finally {
          IOUtils.closeQuietly(bis);
          try {
            Thread.sleep(WAIT_BETWEEN_UPDATES);
          } catch (InterruptedException e) {
          }
        }
      }


    }
  }


  // https://coinmarketcap.com/currencies/bitcoin/#markets
}
