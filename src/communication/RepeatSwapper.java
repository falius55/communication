package communication;

/**
 * 一度の接続で複数回の送受信を行う際に利用するSwapperです。
 * swapメソッド内では、最後の送信内容を返す際にあらかじめfinishメソッドを呼ぶ必要があります。
 *
 * クライアントではisContinueメソッドが受信直後に呼ばれるため、送信直前に呼ばれるswapメソッドの引数は
 * 最後に受信したReceiverオブジェクトを受け取れません。
 * 確実に受信した内容を処理するにはOnReceiveListener#onReceiveメソッドの引数に渡される
 * Receiverオブジェクトを利用するか、あるいはClient#startメソッドの戻り値として取得できる、
 * 最後に受信したデータが格納されたReceiverオブジェクトを利用する必要があります。
 * @author "ymiyauchi"
 *
 */
public abstract class RepeatSwapper implements Swapper {
    private boolean isContinue = true;

    protected final void finish() {
        isContinue = false;
    }

    @Override
    public final boolean doContinue() {
        return isContinue;
    }

}
