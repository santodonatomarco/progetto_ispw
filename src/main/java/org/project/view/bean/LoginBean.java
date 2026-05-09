package org.project.view.bean;

import org.project.ing.enumerations.AuthProvider;

public class LoginBean {
    private String email;
    private String password;
    private AuthProvider authProvider;  // null se login con email/password

    public String getEmail()                   { return email; }
    public String getPassword()                { return password; }
    public AuthProvider getAuthProvider()      { return authProvider; }

    public void setEmail(String email)                    { this.email = email; }
    public void setPassword(String password)              { this.password = password; }
    public void setAuthProvider(AuthProvider provider)    { this.authProvider = provider; }
}