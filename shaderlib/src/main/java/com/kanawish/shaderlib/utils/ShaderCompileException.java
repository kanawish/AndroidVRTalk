package com.kanawish.shaderlib.utils;

/**
 * Signals a problem while compiling shaders.
 */
public class ShaderCompileException extends Exception{
   public ShaderCompileException(String detailMessage) {
      super(detailMessage);
   }

   public ShaderCompileException(String detailMessage, Throwable throwable) {
      super(detailMessage, throwable);
   }

   public ShaderCompileException(Throwable throwable) {
      super(throwable);
   }
}
