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
 */
@UnstableApi
public final class VideoCache {

  private static final String TAG = "VideoCache";
  private static final String CACHE_DIRECTORY_NAME = "exoplayer";

  private static @Nullable DataSource.Factory dataSourceFactory;
  private static @Nullable DefaultHttpDataSource.Factory httpDataSourceFactory;
  private static @Nullable DatabaseProvider databaseProvider;
  private static @Nullable Cache downloadCache;

  /**
   * Returns a {@link DataSource.Factory} that uses caching.
   */
  public static synchronized DataSource.Factory getDataSourceFactory(Context context) {
    if (dataSourceFactory == null) {
      context = context.getApplicationContext();
      DefaultHttpDataSource.Factory upstreamHttpFactory = getHttpDataSourceFactory(context);
      
      dataSourceFactory = new CacheDataSource.Factory()
          .setCache(getDownloadCache(context))
          .setUpstreamDataSourceFactory(upstreamHttpFactory)
          .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);
    }
    return dataSourceFactory;
  }

  /**
   * Returns a {@link DefaultHttpDataSource.Factory}.
   */
  public static synchronized DefaultHttpDataSource.Factory getHttpDataSourceFactory(
      Context context) {
    if (httpDataSourceFactory == null) {
      CookieManager cookieManager = new CookieManager();
      cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
      CookieHandler.setDefault(cookieManager);
      httpDataSourceFactory = new DefaultHttpDataSource.Factory();
    }
    return httpDataSourceFactory;
  }

  /**
   * Returns the download cache.
   */
  private static synchronized Cache getDownloadCache(Context context) {
    if (downloadCache == null) {
      context = context.getApplicationContext();
      File cacheDirectory = new File(context.getCacheDir(), CACHE_DIRECTORY_NAME);
      if (!cacheDirectory.exists()) {
        cacheDirectory.mkdirs();
      }
      downloadCache =
          new SimpleCache(
              cacheDirectory,
              new NoOpCacheEvictor(),
              getDatabaseProvider(context));
    }
    return downloadCache;
  }

  /**
   * Gets the directory where cached content is stored.
   */
  public static synchronized File getCacheDirectory(Context context) {
    File cacheDir = new File(context.getApplicationContext().getCacheDir(), CACHE_DIRECTORY_NAME);
    if (!cacheDir.exists()) {
        cacheDir.mkdirs();
    }
    return cacheDir;
  }

  /**
   * Returns the database provider.
   */
  private static synchronized DatabaseProvider getDatabaseProvider(Context context) {
    if (databaseProvider == null) {
      databaseProvider = new StandaloneDatabaseProvider(context);
    }
    return databaseProvider;
  }

  private VideoCache() {
    // Prevent instantiation
  }
} 