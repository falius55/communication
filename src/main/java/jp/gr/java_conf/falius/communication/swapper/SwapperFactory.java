package jp.gr.java_conf.falius.communication.swapper;

/**
 * Swapperを生成するファクトリメソッドを持つインターフェースです。
 * 主にサーバー側で使います。
 * このインターフェースを実装したクラスのインスタンスはひとつのサーバーインスタンスにつきひとつだけ存在します。
 * @since 1.4.0
 */
public interface SwapperFactory {

    /**
     *
     * @return
     * @since 1.4.0
     */
    Swapper get();
}