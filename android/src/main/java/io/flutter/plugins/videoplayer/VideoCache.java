// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.videoplayer;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.media3.database.DatabaseProvider;
import androidx.media3.database.StandaloneDatabaseProvider;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.cache.Cache;
import androidx.media3.datasource.cache.CacheDataSource;
import androidx.media3.datasource.cache.NoOpCacheEvictor;
import androidx.media3.datasource.cache.SimpleCache;
import androidx.media3.common.util.UnstableApi;
import java.io.File;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import android.util.Log;

/**
 * Utility class that provides caching capabilities for video playback.
 * Uses a strict singleton pattern to ensure only one cache instance exists.
 */
@UnstableApi
public final class VideoCache {

  private static final String TAG = "VideoCache";
  private static final String CACHE_DIRECTORY_NAME = "exoplayer";

  // Singleton instance of this class
  private static volatile VideoCache sInstance;
  
  // Cache components
  private DataSource.Factory dataSourceFactory;
  private DefaultHttpDataSource.Factory httpDataSourceFactory;
  private DatabaseProvider databaseProvider;
  private Cache downloadCache;
  
  /**
   * Gets the singleton instance of VideoCache.
   * 
   * @param context The application context
   * @return The VideoCache singleton instance
   */
  public static VideoCache getInstance(Context context) {
    if (sInstance == null) {
      synchronized (VideoCache.class) {
        if (sInstance == null) {
          sInstance = new VideoCache(context.getApplicationContext());
        }
      }
    }
    return sInstance;
  }

  /**
   * Private constructor that initializes the cache.
   * 
   * @param context The application context
   */
  private VideoCache(Context context) {
    // Initialize cache components
    this.databaseProvider = new StandaloneDatabaseProvider(context);
    
    // Initialize cache
    File cacheDirectory = new File(context.getCacheDir(), CACHE_DIRECTORY_NAME);
    if (!cacheDirectory.exists()) {
      cacheDirectory.mkdirs();
    }
    
    this.downloadCache = new SimpleCache(
        cacheDirectory,
        new NoOpCacheEvictor(),
        this.databaseProvider);
        
    // Initialize HTTP data source factory
    CookieManager cookieManager = new CookieManager();
    cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    CookieHandler.setDefault(cookieManager);
    this.httpDataSourceFactory = new DefaultHttpDataSource.Factory();
    
    // Create the cached data source factory
    this.dataSourceFactory = new CacheDataSource.Factory()
        .setCache(this.downloadCache)
        .setUpstreamDataSourceFactory(this.httpDataSourceFactory)
        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);
  }

  /**
   * Returns a {@link DataSource.Factory} that uses caching.
   */
  public DataSource.Factory getDataSourceFactory() {
    return dataSourceFactory;
  }

  /**
   * Returns a {@link DefaultHttpDataSource.Factory}.
   */
  public DefaultHttpDataSource.Factory getHttpDataSourceFactory() {
    return httpDataSourceFactory;
  }

  /**
   * Gets the directory where cached content is stored.
   */
  public File getCacheDirectory(Context context) {
    return new File(context.getApplicationContext().getCacheDir(), CACHE_DIRECTORY_NAME);
  }
  
  /**
   * Releases the cache resources when they're no longer needed.
   * Should be called when the app is being destroyed.
   */
  public void release() {
    try {
      if (downloadCache != null) {
        downloadCache.release();
        downloadCache = null;
      }
    } catch (Exception e) {
      Log.e(TAG, "Error releasing cache", e);
    }
  }
  
  /**
   * Utility method to get the data source factory from the singleton instance.
   */
  public static DataSource.Factory getDataSourceFactory(Context context) {
    return getInstance(context).getDataSourceFactory();
  }
} 