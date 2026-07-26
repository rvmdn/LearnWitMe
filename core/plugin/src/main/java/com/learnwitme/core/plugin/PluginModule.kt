package com.learnwitme.core.plugin

import com.learnwitme.core.navigation.ScreenProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class PluginModule {

    @Binds
    @IntoSet
    abstract fun bindAiPlugin(plugin: AiPlugin): MetaPlugin

    @Binds
    @IntoSet
    abstract fun bindOcrPlugin(plugin: OcrPlugin): MetaPlugin

    @Binds
    @IntoSet
    abstract fun bindAiScreenProvider(provider: AiScreenProvider): ScreenProvider

    @Binds
    @IntoSet
    abstract fun bindOcrScreenProvider(provider: OcrScreenProvider): ScreenProvider
}
