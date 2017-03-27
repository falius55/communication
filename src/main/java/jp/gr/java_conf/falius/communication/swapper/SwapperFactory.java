package jp.gr.java_conf.falius.communication.swapper;

/**
 * Swapperを生成するファクトリメソッドを持つインターフェースです。
 * このインターフェースを実装したクラスは
 */
public interface SwapperFactory {

    /**
     *
     * @return
     */
    Swapper get();
}