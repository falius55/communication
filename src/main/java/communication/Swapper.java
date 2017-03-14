package communication;

import communication.receiver.Receiver;
import communication.sender.Sender;

/**
 * <p>
 * 受信内容を受け取り、送信内容を格納したSenderオブジェクトを作成するクラスのインターフェースです。
 *
 *<p>
 * 受信内容をもとに送信内容を決定するためにReceiverオブジェクトを引数に受け取りますが、
 * クライアントにおいては最初の一度はreceiverにnullが入り、最後の受信内容はswap()メソッドで
 * 受け取れないなど不便なものとなっています。そのため、Swapperは純粋にSenderオブジェクトを生成するための
 * オブジェクトとし、受信内容の処理は一度の送受信ならClient#startメソッドの戻り値、複数の送受信を行うなら
 * OnReceiveListener#onReceiveの引数に渡されるReceiverオブジェクトを利用するのが確実です。
 *
 *<p>
 * サーバーとクライアントが同じ回数だけswapメソッドが呼ばれるようにisContinueメソッドがfalseを返すように調整する
 * ことで、お互いに受信失敗が発生せず自然に通信を終えることができます。
 *
 * @author "ymiyauchi"
 *
 */
public interface Swapper {

    /**
     * <p>
     * データの受信と送信を繋ぐ処理を行うクラスのインターフェースです。
     * 受信データが格納されたReceiverからデータを取得し、
     * 送信するデータを格納したSenderオブジェクトを作成して戻り値としてください
     *
     *<p>
     * swapメソッドは送信の直前に実行されます。そのため、受信直後に実行され
     * るOnReceiveListener#onReceiveメソッドにて消費された受信データは
     * このメソッドに渡されるReceiverオブジェクトには入っていません。
     *
     *<p>
     * nullを返すと通信を強制的に終了します。
     * この場合、クライアントかサーバーかに関係なく送信直前に接続を切断します。
     * ただし通信相手は受信に失敗したことを検知して接続を切ることになるため、正常に終了したとは言えないかもしれません。
     *
     *<p>
     * クライアントに限り、最初の一度だけreceiverにnullが渡されますので注意してください。
     *
     */
    Sender swap(String remoteAddress, Receiver receiver);

    /**
     * 通信を続けるかどうかを返すメソッドです。
     * このメソッドはクライアントでは受信直後、サーバーでは送信直後に呼ばれ、falseとなった時点で接続を切断します。
     * @return 通信を続けるかどうか
     */
    boolean doContinue();

    interface SwapperFactory {

        Swapper get();
    }
}