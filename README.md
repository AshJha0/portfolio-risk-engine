# Portfolio Risk Engine

A multi-module Java/Maven market risk platform: Historical/Parametric/Monte Carlo VaR,
Expected Shortfall (CVaR), stress testing, scenario analysis, risk contribution /
incremental / marginal VaR, Greeks-based option risk, correlated Monte Carlo via
Cholesky decomposition, a REST API, and a web dashboard.

## Module map

```
portfolio-risk-engine
├── risk-core        Domain model (Position, Portfolio, MarketData), pricing
│                     (Black-Scholes, Garman-Kohlhagen, + Heston/SABR/Dupire
│                     extension interfaces), correlation matrix / Cholesky utility
├── var-engine        Historical VaR, Parametric VaR, Monte Carlo VaR (independent
│                     and correlated multi-asset simulation)
├── stress-engine     Stress testing (2008 Crisis, COVID Crash presets, custom shocks)
│                     and scenario analysis (arbitrary what-if factor shocks)
├── analytics         Expected Shortfall, risk contribution (Component/Incremental/
│                     Marginal VaR), Greeks engine (Delta/Gamma/Vega VaR), risk reports
├── regulatory        FRTB liquidity horizons and Expected Shortfall capital charge
├── realtime          LMAX Disruptor ring buffer + Kafka consumer for intraday
│                     low-latency market data -> VaR pipeline
├── rest-api          Spring Boot REST API: POST /risk/var, POST /risk/stress
└── dashboard         Static web dashboard (Spring Boot static resource server)
                     consuming the rest-api module
```

## Requirements

- JDK 17+
- Maven 3.8+
- Network access to Maven Central (for first build, to download dependencies)

## Build

```bash
mvn clean install
```

This builds every module in dependency order and runs all unit tests
(`risk-core`, `var-engine`, `stress-engine`, `analytics` each have JUnit 5 tests).

To build a single module and its dependencies only:

```bash
mvn -pl var-engine -am clean install
```

## Run

Start the REST API (listens on port 8080):

```bash
mvn -pl rest-api spring-boot:run
```

In a second terminal, start the dashboard (listens on port 8081):

```bash
mvn -pl dashboard spring-boot:run
```

Then open `http://localhost:8081` in a browser and click **Run risk calculation**.
The dashboard's default sample portfolio (AAPL equity + EURUSD FX) is sent to the
REST API; click "Run risk calculation" again after changing the scenario dropdown
to see different stress test results.

### Calling the API directly

```bash
curl -X POST http://localhost:8080/risk/var \
  -H "Content-Type: application/json" \
  -d '{
    "portfolioName": "Test Book",
    "positions": [
      {"symbol":"AAPL","assetClass":"EQUITY","quantity":1000,"marketPrice":200.0}
    ],
    "marketData": [
      {"symbol":"AAPL","price":200.0,"volatility":0.02,"interestRate":0.04}
    ],
    "historicalPnL": [-50000,-30000,-10000,0,10000,30000,50000],
    "portfolioVolatility": 0.015,
    "monteCarloScenarios": 10000
  }'

curl -X POST http://localhost:8080/risk/stress \
  -H "Content-Type: application/json" \
  -d '{
    "portfolioName": "Test Book",
    "scenario": "2008 Crisis",
    "positions": [
      {"symbol":"SPX","assetClass":"EQUITY","quantity":100,"marketPrice":5000.0}
    ]
  }'
```

## What's fully implemented vs. scaffolded

**Fully implemented and unit-tested:**
- Historical, Parametric, and Monte Carlo VaR (including correlated multi-asset Monte
  Carlo via Cholesky decomposition of a correlation matrix)
- Expected Shortfall / CVaR
- Stress testing (named presets + custom shocks, applied per asset class) and
  scenario analysis (arbitrary what-if factor shocks)
- Risk contribution: Component VaR, Incremental VaR, Marginal VaR
- Greeks engine: Black-Scholes and Garman-Kohlhagen pricing with Delta/Gamma/Vega/
  Theta/Rho, and Delta/Gamma/Vega VaR aggregation
- REST API (`POST /risk/var`, `POST /risk/stress`) and web dashboard
- Real-time pipeline: a genuinely wired LMAX Disruptor ring buffer + Kafka consumer
  feeding the shared market data cache (see `realtime` module)
- FRTB liquidity horizons (per Basel Committee standardized table) and a simplified
  Expected Shortfall capital charge calculation

**Scaffolded as documented extension points, not fully implemented:**
- Heston, SABR, and Dupire local volatility models (`risk-core/pricing`) — these are
  each substantial standalone numerical-methods projects (calibration, characteristic-
  function Fourier inversion, PDE solvers). Interfaces and detailed Javadoc describing
  exactly what's needed are provided so a real implementation can be dropped in later.
- Full FRTB cross-bucket, cross-horizon correlation aggregation — the regulatory module
  implements the core per-horizon scaling formula but not the complete multi-bucket
  capital aggregation, which requires a much larger correlation/bucketing framework.
