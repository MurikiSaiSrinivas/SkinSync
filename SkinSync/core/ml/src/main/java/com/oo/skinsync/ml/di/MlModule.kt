package com.oo.skinsync.ml.di

import com.oo.skinsync.ml.DownscaleGarmentColorExtractor
import com.oo.skinsync.ml.FaceColorExtractor
import com.oo.skinsync.ml.GarmentColorExtractor
import com.oo.skinsync.ml.MediaPipeFaceColorExtractor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MlModule {
    @Binds
    @Singleton
    abstract fun bindFaceColorExtractor(impl: MediaPipeFaceColorExtractor): FaceColorExtractor

    @Binds
    @Singleton
    abstract fun bindGarmentColorExtractor(impl: DownscaleGarmentColorExtractor): GarmentColorExtractor
}
