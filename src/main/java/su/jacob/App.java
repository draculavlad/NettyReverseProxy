package su.jacob;

import su.jacob.socks.ProxyServer;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        String remoteIp = "";
        int remotePort = 0;
        String localIp = "";
        int localPort = 0;
        if (args.length == 4) {
            remoteIp = args[0];
            remotePort = Integer.valueOf(args[1]);
            localIp = args[2];
            localPort = Integer.valueOf(args[3]);
        } else {
            System.exit(1);
        }
        ProxyServer proxyServer = new ProxyServer(remoteIp, remotePort, localIp, localPort);
        proxyServer.start();
    }
}
