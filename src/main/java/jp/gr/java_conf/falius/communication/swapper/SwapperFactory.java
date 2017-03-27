package jp.gr.java_conf.falius.communication.swapper;

/**
 * Swapperを生成するファクトリメソッドを持つインターフェースです。
 */
public interface SwapperFactory {

    Swapper get();
}