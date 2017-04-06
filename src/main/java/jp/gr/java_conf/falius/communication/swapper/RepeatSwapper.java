package jp.gr.java_conf.falius.communication.swapper;

/**
 * 一度の接続で複数回の送受信を行う際に利用するSwapperです。
 * swapメソッド内では、最後の送信内容を返す際にあらかじめfinishメソッドを呼ぶ必要があります。
 *
 * クライアントではdoContinueメソッドが受信直後に呼ばれるため、送信直前に呼ばれるswapメソッドの引数は
 * 最後に受信したReceiveDataオブジェクトを受け取れません。
 * 確実に受信した内容を処理するにはOnReceiveListener#onReceiveメソッドの引数に渡される
 * ReceiveDataオブジェクトを利用するか、あるいはClient#startメソッドの戻り値として取得できる、
 * 最後に受信したデータが格納されたReceiveDataオブジェクトを利用する必要があります。
 * @author "ymiyauchi"
 *
 */
public abstract class RepeatSwapper implements Swapper {
    private volatile boolean isContinue = true;

    /**
     *
     * このメソッドを呼び出すことで、doContinueメソッドがfalseを
     * 返すようになって通信が終了します。
     * 送信、受信共にswapメソッドが同数呼ばれるようにしてこのメソッドを
     * 呼び出してください。
     * サーバーはその直後、クライアントはもう一度受信した上で接続が切断されます。
     */
    protected final void finish() {
        isContinue = false;
    }

    @Override
    public final boolean doContinue() {
        return isContinue;
    }

}
