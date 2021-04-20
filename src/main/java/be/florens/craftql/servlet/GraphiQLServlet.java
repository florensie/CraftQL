package be.florens.craftql.servlet;

import be.florens.craftql.CraftQL;
import net.fabricmc.loader.api.FabricLoader;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GraphiQLServlet extends HttpServlet {

    private static final byte[] graphiqlResp = readGraphiQLResource();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentLength(graphiqlResp.length);
        resp.getOutputStream().write(graphiqlResp);
    }

    private static byte[] readGraphiQLResource() {
        Path path = FabricLoader.getInstance().getModContainer(CraftQL.MOD_ID)
                .orElseThrow(() -> new RuntimeException("Could not find own mod container"))
                .getPath("graphiql.html");

        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException("Could not read graphiql.html");
        }
    }
}
