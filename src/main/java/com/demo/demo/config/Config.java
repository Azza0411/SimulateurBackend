package com.demo.demo.config;

import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * Config statique (miroir Python Config).
 * TIMEFRAMES pour sync (1h/4h), UPDATE_INTERVAL pour scheduler (300s = 5min).
 * Changez ici pour sync (1er "1h", 2em "4h", global 300).
 */
@Configuration
public class Config {
    public static final List<String> PAIRS = Arrays.asList("EURUSD=X", "USDJPY=X", "GBPUSD=X", "EURJPY=X", "AUDUSD=X", "USDCAD=X");
    public static final List<String> TIMEFRAMES = Arrays.asList("1h", "4h"); // 1er "1h", 2em "4h"
    public static final int UPDATE_INTERVAL = 300; // Global 5min (secondes)
    public static final String EXPORT_DIR = "./trading_exports/";
    //energy
    public static final List<String> ENERGY_SYMBOLS = Arrays.asList("XOM", "CVX", "COP", "SLB", "OXY", "BP");

//matiere premiere
public static final List<String> COMMODITY_SYMBOLS = Arrays.asList("CL", "GC", "SI", "HG", "NG", "ZB");
//imobiler
public static final List<String> REAL_ESTATE_SYMBOLS = Arrays.asList("PLD", "AMT", "CCI", "EQIX", "PSA", "SPG");
   // NOUVEAU : Symbols pour TECHNOLOGIE (Alpha Vantage)
   public static final List<String> TECH_SYMBOLS = Arrays.asList(
           "AAPL",  // Apple (iPhone, services)
           "MSFT",  // Microsoft (Azure, Office)
           "GOOGL", // Google (search, AI)
           "NVDA",  // Nvidia (GPU, AI chips)
           "TSLA",  // Tesla (EV, auto tech)
           "AMZN",  // Amazon (e-commerce, AWS)
           "META",  // Meta (Facebook, VR)
           "AMD",   // AMD (processeurs)
           "INTC",  // Intel (semi-conducteurs)
           "CRM"    // Salesforce (CRM cloud)
   );


    public static final int EMA_SHORT = 9;
    public static final int EMA_LONG = 21;
    public static final int RSI_PERIOD = 14;
    public static final int MACD_FAST = 12;
    public static final int MACD_SLOW = 26;
    public static final int MACD_SIGNAL = 9;
}
