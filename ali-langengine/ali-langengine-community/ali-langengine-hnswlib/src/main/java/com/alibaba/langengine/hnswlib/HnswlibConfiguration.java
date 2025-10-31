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
package com.alibaba.langengine.hnswlib;


public class HnswlibConfiguration {

    /**
     * Hnswlib 默认存储目录
     */
    public static String HNSWLIB_STORAGE_PATH = System.getProperty("hnswlib.storage.path", "/tmp/hnswlib");

    /**
     * Hnswlib 默认最大元素数量
     */
    public static int HNSWLIB_MAX_ELEMENTS = Integer.parseInt(System.getProperty("hnswlib.max.elements", "10000"));

    /**
     * Hnswlib 默认向量维度
     */
    public static int HNSWLIB_DIMENSION = Integer.parseInt(System.getProperty("hnswlib.dimension", "768"));

    /**
     * Hnswlib 默认 M 参数
     */
    public static int HNSWLIB_M = Integer.parseInt(System.getProperty("hnswlib.m", "16"));

    /**
     * Hnswlib 默认 ef_construction 参数
     */
    public static int HNSWLIB_EF_CONSTRUCTION = Integer.parseInt(System.getProperty("hnswlib.ef.construction", "200"));

    /**
     * Hnswlib 默认 ef 参数
     */
    public static int HNSWLIB_EF = Integer.parseInt(System.getProperty("hnswlib.ef", "10"));
}
