/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.suite;

import com.alibaba.langengine.alphavantage.AlphaVantageToolTest;
import com.alibaba.langengine.alphavantage.AlphaVantageClientTest;
import com.alibaba.langengine.alphavantage.AlphaVantageToolFactoryTest;
import com.alibaba.langengine.yahoofinance.YahooFinanceToolTest;
import com.alibaba.langengine.yahoofinance.YahooFinanceClientTest;
import com.alibaba.langengine.yahoofinance.YahooFinanceToolFactoryTest;
import com.alibaba.langengine.integration.FinanceToolsIntegrationTest;
import com.alibaba.langengine.stress.FinanceToolsStressTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * 金融工具完整测试套件
 * 
 * @author langengine
 */
@Suite
@SelectClasses({
    // Alpha Vantage 测试
    AlphaVantageToolTest.class,
    AlphaVantageClientTest.class,
    AlphaVantageToolFactoryTest.class,
    
    // Yahoo Finance 测试
    YahooFinanceToolTest.class,
    YahooFinanceClientTest.class,
    YahooFinanceToolFactoryTest.class,
    
    // 集成测试
    FinanceToolsIntegrationTest.class,
    
    // 压力测试
    FinanceToolsStressTest.class
})
public class FinanceToolsTestSuite {
    // 测试套件类，不需要实现任何方法
}
