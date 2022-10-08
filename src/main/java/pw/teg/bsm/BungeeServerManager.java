package pw.teg.bsm;

import express.Express;
import net.md_5.bungee.api.plugin.Plugin;
import pw.teg.bsm.api.ServerManagerAPI;
import pw.teg.bsm.commands.ServerManagerCommand;

import java.lang.reflect.Array;
import java.net.InetSocketAddress;

import static pw.teg.bsm.util.ServerHelper.serverExists;

public class BungeeServerManager extends Plugin {

    private static BungeeServerManager instance;
    private static ServerManagerAPI api;

    public Express app = new Express();

    @Override
    public void onEnable() {
        instance = this;

        api = new ServerManagerAPI();

        getProxy().getPluginManager().registerCommand(this, new ServerManagerCommand());


        app.post("/addServer", (req, res) -> {
            String errors = "";
            if (serverExists(req.getFormQuery("serverName"))) {
                errors = errors + "A server with this name exist.";
            }
            if (String.valueOf(api.listServers()).replace("localhost/", "").contains(
                    req.getFormQuery("ip") + ":" + req.getFormQuery("port"))) {
                if (errors == "") errors = errors + "" + "A server with this address exist.";
                else errors = errors + " & " + "A server with this address exist.";
            }
            if (errors == "") {
                try {
                    api.addServer(
                            req.getFormQuery("serverName"),
                            new InetSocketAddress(
                                    req.getFormQuery("ip"),
                                    Integer.parseInt(req.getFormQuery("port"))),
                            req.getFormQuery("motd"),
                            Boolean.parseBoolean(req.getFormQuery("restricted"))
                    );
                    res.send("Server created.");
                } catch(Exception e) {
                    res.send(String.valueOf(e));
                }}
            else res.send(errors);
        });

        app.post("/removeServer", (req, res) -> {
            String errors = "";
            if (!serverExists(req.getFormQuery("serverName"))) {
                errors = errors + "The server does not exist!";
            }
            if (errors == "") {
                try {
                    api.removeServer(req.getFormQuery("serverName"));
                    res.send("The server has been successfully deleted!");
                } catch (Exception e) {
                    res.send(String.valueOf(e));
                }} else res.send(errors);
        });

        app.get("/serverExists/:value", (req, res) -> {
            res.send(String.valueOf(api.serverExists(req.getParam("value"))));
        });

        app.get("/listServers", (req, res) -> {
            res.send(String.valueOf(api.listServers()));
        });

        app.listen(8502);
    }

    public static BungeeServerManager get() {
        return instance;
    }

    public static ServerManagerAPI getApi() {
        return api;
    }
}
