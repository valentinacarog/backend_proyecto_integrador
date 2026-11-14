package com.cesde.proyectointegrador.modules.auth.dto;

import com.cesde.proyectointegrador.modules.usuarios.entity.Usuario;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

public class AuthResponse {
    private boolean success = true;
    private String token;
    private Usuario user;

    public AuthResponse() {}

    public AuthResponse(String token, Usuario user) {
        this.success = true;
        this.token = token;
        this.user = user;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    @JsonProperty("accessToken")
    public String getAccessToken() { return token; }

    public Usuario getUser() { return user; }
    public void setUser(Usuario user) { this.user = user; }

    @JsonProperty("usuario")
    public Usuario getUsuario() { return user; }

    @JsonProperty("data")
    public Map<String, Object> getData() {
        Map<String, Object> map = new HashMap<>();
        map.put("token", token != null ? token : "");
        map.put("user", user);
        map.put("usuario", user);
        return map;
    }
}
