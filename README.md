# video_player_android

[![pub package](https://img.shields.io/pub/v/video_player_android.svg)](https://pub.dev/packages/video_player_android)

The Android implementation of [`video_player`][1].

## Usage

This package is [endorsed][2], which means you can simply use `video_player`
normally. This package will be automatically included in your app when you do,
so you do not need to add it to your `pubspec.yaml`.

However, if you `import` this package to use any of its APIs directly, you
should add it to your `pubspec.yaml` as usual.

## Caching Support

This implementation includes built-in video caching capabilities using ExoPlayer's cache system. When videos are played, they are automatically cached to the device's storage and will be played from cache when available in future playback sessions.

### Cache Features

- Videos are automatically cached when played
- Cached videos are stored in the standard ExoPlayer cache directory: `/data/data/[package_name]/cache/exoplayer`
- No additional configuration is needed - caching happens automatically

### How Caching Works

The implementation uses:
- A `SimpleCache` instance with a `NoOpCacheEvictor` which means cached content is not automatically cleared
- A `CacheDataSource.Factory` that handles reading from and writing to the cache
- The standard ExoPlayer cache location for maximum compatibility

## Additional Customization

If you want to customize the caching behavior, you can modify the `VideoCache.java` file which contains the caching implementation.

[1]: https://pub.dev/packages/video_player
[2]: https://flutter.dev/to/endorsed-federated-plugin
