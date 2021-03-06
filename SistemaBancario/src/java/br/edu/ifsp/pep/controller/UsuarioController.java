/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.edu.ifsp.pep.controller;

import br.edu.ifsp.pep.dao.UsuarioDAO;
import br.edu.ifsp.pep.model.Usuario;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author aluno
 */
@Named
@SessionScoped
public class UsuarioController implements Serializable {

    @Inject
    private UsuarioDAO usuarioDAO;
    private Usuario usuario;
    private Usuario usuarioLogado;
    private Usuario uSelecionado;
    private List<Usuario> usuarios;

    public UsuarioController() {
        System.out.println("construtor usuario.");
        this.uSelecionado = null;
        this.usuario = new Usuario();
    }

    public UsuarioDAO getUsuarioDAO() {
        return usuarioDAO;
    }

    public void setUsuarioDAO(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuarioLogado() {
        return usuarioLogado;
    }

    public void setUsuarioLogado(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
    }

    public Usuario getuSelecionado() {
        return uSelecionado;
    }

    public void setuSelecionado(Usuario uSelecionado) {
        this.uSelecionado = uSelecionado;
    }

    public List<Usuario> getUsuarios() {
        if (this.usuarios == null) {
            this.usuarios = usuarioDAO.buscarTodos();
        }
        return usuarios;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    public void redirecionarPagina(String pagina) {
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("./" + pagina);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void retornarHome() {
        if (usuarioLogado.getNivelAcesso().equals("Cliente")) {
            redirecionarPagina("Tela_Principal_Cliente.xhtml");
        } else if (usuarioLogado.getNivelAcesso().equals("Funcionario")) {
            redirecionarPagina("Tela_Principal_Funcionario.xhtml");
        } else if (usuarioLogado.getNivelAcesso().equals("Administrador")) {
            redirecionarPagina("Tela_Principal_Administrador.xhtml");
        }
    }

    public void autenticar() {
        usuarioLogado = usuarioDAO.buscarPorCPFSenha(usuario.getCpf(), usuario.getSenha(), usuario.getNivelAcesso());

        System.out.println("\nCPF: " + this.usuario.getCpf()
                + "\nSenha: " + usuario.getSenha()
                + "\nNivel de acesso: " + usuario.getNivelAcesso());
        if (this.usuarioLogado != null) {
            System.out.println("Autenticado");
            addMessage(FacesMessage.SEVERITY_INFO, "Informa????o", "Usu??rio autenticado.");
            retornarHome();
        } else {
            System.out.println("N??o autenticado");
            addMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Usu??rio n??o autenticado.");
            this.usuario = new Usuario();
        }
    }

    public void deslogar() {
        if (this.usuarioLogado != null) {
            this.usuarioLogado = null;
            redirecionarPagina("Tela_Login.xhtml");
        } else {
            //quando d?? o bug de sair da sess??o e ficar dentro da tela principal ainda
            redirecionarPagina("Tela_Login.xhtml");
        }
    }

    public void adicionar() {

        /*
            3 situa????es: 
            
            1 - ADM e Funcionario podem cadastrar clientes.
            2 - ADM cadastra ADMs, Funcion??rios e Clientes.
            3 - Se um Funcion??rio tentar cadastrar um ADM 
            ou Funcion??rio, vai dar uma mensagem de erro.
        */
        if (usuarioLogado.getNivelAcesso().equals("Funcionario")) {
                
            if (this.usuario.getNivelAcesso().equals("Cliente")) {
                if (this.usuario.getEmail().isEmpty()) {
                    this.usuario.setEmail("N??o informado");
                }
                usuarioDAO.insert(usuario);
                this.usuarios = null;
                System.out.println("adicionou um usuario na lista.");
                addMessage(FacesMessage.SEVERITY_INFO, "Informa????o", "Cadastro realizado.");
            }
        } 

        if(this.usuarioLogado.getNivelAcesso().equals("Administrador")){
            if (this.usuario.getNivelAcesso().equals("Administrador")||
                this.usuario.getNivelAcesso().equals("Funcionario")||
                this.usuario.getNivelAcesso().equals("Cliente")) {
                if (this.usuario.getEmail().isEmpty()) {
                    this.usuario.setEmail("N??o informado");
                }
                usuarioDAO.insert(usuario);
                this.usuarios = null;
                System.out.println("adicionou um usuario na lista.");
                addMessage(FacesMessage.SEVERITY_INFO, "Informa????o", "Cadastro realizado.");
            }
        }

        if(usuarioLogado.getNivelAcesso().equals("Funcionario")){
             if (usuario.getNivelAcesso().equals("Funcionario") 
            || usuario.getNivelAcesso().equals("Administrador")) {
                addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Apenas um Administrador pode Cadastrar um "
                + "usu??rio de n??vel de acesso de Funcion??rio ou Administrador.");
            }
        }
    }

    public void excluir() {
        if (uSelecionado != null) {
            usuarioDAO.delete(uSelecionado);
            this.usuarios = null;
            addMessage(FacesMessage.SEVERITY_INFO, "Informa????o", "Exclus??o realizada");
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Informa????o", "Selecione um usuario para excluir");
        }
    }

    public char labelAvatar(){
        String nome = this.usuarioLogado.getNome();
        char inicial = '?';

        if(usuarioLogado != null){
            inicial = nome.charAt(0);
            return inicial;
        }else{
            redirecionarPagina("Tela_Login.xhtml");       
        }
       return inicial;
    }

    public void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().
                addMessage(null, new FacesMessage(severity, summary, detail));
    }

}
