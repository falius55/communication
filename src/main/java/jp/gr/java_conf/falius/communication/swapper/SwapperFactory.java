package jp.gr.java_conf.falius.communication.swapper;

/**
 * Swapperを生成するファクトリメソッドを持つインターフェースです。
 * 主にサーバー側で使います。
 * このインターフェースを実装したクラスのインスタンスはひとつのサーバーインスタンスにつきひとつだけ存在します。
 */
public interface SwapperFactory {

    /**
     *
     * @return
     */
    Swapper get();
}