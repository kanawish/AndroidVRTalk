package com.kanawish.androidvrtalk;

import com.kanawish.androidvrtalk.domain.DomainModule;
import com.kanawish.androidvrtalk.ui.UiModule;
import com.kanawish.shaderlib.ShaderLibModule;

// TODO: Each build flavour will get it's own Modules class. (Instead of 1 copy living in "main")
final class Modules {
  static Object[] list(VrTalkApp app) {
    return new Object[] {
        new AppModule(app),
        new UiModule(),
        new DomainModule(),
        new ShaderLibModule()
    };
  }

  private Modules() {
    // No instances.
  }
}
