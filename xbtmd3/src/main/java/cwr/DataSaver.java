package cwr;

import cwr.exchanges.CoinMarketCapRecord;
import cwr.exchanges.Exchange;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.trade.LimitOrder;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataSaver {

  public static final String BITCOIN_MARKET_PRICES = "bitcoin_market_prices";
  public static final String BITCOIN_MARKET_PRICES_RP = "bitcoin_market_prices_rp";

  private static DecimalFormat moneyFormatter = new DecimalFormat("0.000000");
  private final InfluxDB influxDB;

  public DataSaver() {
    this.influxDB = InfluxDBFactory.connect("http://127.0.0.1:8086");
    influxDB.enableBatch(500, 20, TimeUnit.SECONDS).disableGzip().setLogLevel(InfluxDB.LogLevel.FULL);
    if (!influxDB.describeDatabases().contains(BITCOIN_MARKET_PRICES)) {
      influxDB.createDatabase(BITCOIN_MARKET_PRICES);
      Query query = new Query("CREATE RETENTION POLICY \""+BITCOIN_MARKET_PRICES_RP+"\" ON \"bitcoin_market_prices\" DURATION 90d REPLICATION 1 DEFAULT", BITCOIN_MARKET_PRICES);
      influxDB.query(query);
    }

  }

  private BigDecimal[] orderBookToLevels(List<LimitOrder> orders) {
    BigDecimal[] res = new BigDecimal[5];
    BigDecimal[] limits = {new BigDecimal(0), new BigDecimal(1.0), new BigDecimal(10.0), new BigDecimal(100), new BigDecimal(1000)};
    BigDecimal acc = new BigDecimal(0);
    int curLevel = 0;
    // this quirky logic handles cases where one huge order fills multiple levels
    for (int i = 0; i < orders.size() && curLevel < res.length; ) {
      LimitOrder o = orders.get(i);
      acc = acc.add(o.getTradableAmount());
      if (acc.compareTo(limits[curLevel]) == 1) {
        res[curLevel] = o.getLimitPrice();
        curLevel++;
      } else {
        i++;
      }
    }

    return res;
  }

  private List<LimitOrder> orderBookRemoveDust(List<LimitOrder> orders, double dustTreshold, Integer maxLevels) {
    BigDecimal th = new BigDecimal(dustTreshold);
    Stream<LimitOrder> stream = orders.stream().filter(x -> x.getLimitPrice().compareTo(th) >= 0);
    if (maxLevels != null) {
      stream = stream.limit(maxLevels);
    }
    return stream.collect(Collectors.toList());
  }

  private static String limitOrderToShortStr(LimitOrder order) {
    return moneyFormatter.format(order.getLimitPrice()) + "," + order.getTradableAmount();
  }

  public void saveTrade(Exchange exchange, Trade trade) {
    System.out.println(exchange.getClass().getSimpleName() + ": " + trade.toString());
  }

  public void saveOrderBook(Exchange exchange, OrderBook orderBook) {
    List<LimitOrder> askLimitOrders = orderBookRemoveDust(orderBook.getAsks(), 0.0001, 5);
    List<LimitOrder> bidLimitOrders = orderBookRemoveDust(orderBook.getBids(), 0.0001, 5);
    BigDecimal spread = null;
    if (!askLimitOrders.isEmpty() && !bidLimitOrders.isEmpty()) {
      spread = Collections.min(askLimitOrders).getLimitPrice().subtract(Collections.max(bidLimitOrders).getLimitPrice());
    }
    String asks = askLimitOrders.stream().sorted((x, y) -> -(x.getLimitPrice().compareTo(y.getLimitPrice()))).map(DataSaver::limitOrderToShortStr).collect(Collectors.joining(";"));
    String bids = bidLimitOrders.stream().sorted((x, y) -> x.getLimitPrice().compareTo(y.getLimitPrice())).map(DataSaver::limitOrderToShortStr).collect(Collectors.joining(";"));
    String askLevels = Arrays.stream(orderBookToLevels(orderBook.getAsks())).map(x -> x == null ? "" : moneyFormatter.format(x)).collect(Collectors.joining(";"));
    String bidLevels = Arrays.stream(orderBookToLevels(orderBook.getBids())).map(x -> x == null ? "" : moneyFormatter.format(x)).collect(Collectors.joining(";"));
    System.out.println(exchange.getClass().getSimpleName() + ": " + bids + " | " + asks);
    System.out.println(exchange.getClass().getSimpleName() + ": " + bidLevels + "| " + (spread != null ? moneyFormatter.format(spread) : "?") + " |" + askLevels);
  }

  public void saveTicker(Exchange exchange, Ticker ticker) {
    System.out.println(exchange.getClass().getSimpleName() + ": " + ticker.toString());
  }

  public void saveCoinMarketCapPrice(String currency, CoinMarketCapRecord r) {

    Point point1 = Point.measurement("coinmarketcap")
        .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
        .tag("exchange", r.exchange)
        .tag("pair", r.pair)
        .tag("exchange_pair", r.exchange+","+r.pair)
        .tag("no_fees", r.no_fees ? "Y" : "N")
        .tag("volume_magnitude", "" + Math.round(Math.log10(r.volume_usd)))
        .addField("volume_btc", r.volume_btc)
        .addField("volume_usd", r.volume_usd)
        .addField("volume_native", r.volume_native)
        .addField("price_btc", r.price_btc)
        .addField("price_usd", r.price_usd)
        .addField("price_native", r.price_native)
        .build();

    influxDB.write(BITCOIN_MARKET_PRICES, BITCOIN_MARKET_PRICES_RP, point1);

  }
}
