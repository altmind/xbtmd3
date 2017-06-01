package cwr.exchanges;

public enum CoinMarketCapCurrencies {
  BITCOIN("bitcoin", "Bitcoin", "BTC"),
  ETHERIUM("ethereum", "Etherium", "ETH"),
  RIPPLE("ripple", "Ripple", "XRP"),
  NEM("nem", "NEM", "NEM"),
  ETHERIUM_CLASSIC("ethereum-classic", "Etherium Classic", "ETC"),
  LITECOIN("litecoin", "Litecoin", "LTC"),
  DASH("dash", "Dash", "DSH"),
  MONERO("monero", "Monero", "XMR"),
  STRATIS("stratis", "Stratis", "STR"),
  BYTECOIN("bytecoin-bcn", "Bytecoin", "BCN"),
  GOLEM("golem-network-tokens", "Golem", "GNT"),
  STELLAR("stellar", "Stellar Lumens", "XLM"),
  ZCASH("zcash", "ZCash", "ZEC"),
  WAVES("waves", "Waves", "WAVES"),
  DOGECOIN("dogecoin", "Dogecoin", "DOGE"),
  GNOSIS("gnosis-gno", "Gnosis", "GNO"),
  AUGUR("augur", "Augur", "REP");

  public final String slug;
  public final String name;
  public final String symbol;

  private CoinMarketCapCurrencies(final String slug, final String name, final String symbol) {
    this.slug = slug;
    this.name = name;
    this.symbol = symbol;
  }


  @Override
  public String toString() {
    return name;
  }
}
