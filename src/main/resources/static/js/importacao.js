/*
 * Tela de importacao em massa do efetivo.
 *
 * Conversa com POST /api/pessoas/importacao. O token fica em sessionStorage (e nao localStorage)
 * para morrer quando a aba fecha — e uma tela de uso pontual, num computador provavelmente
 * compartilhado na secao.
 */
(function () {
    'use strict';

    var CHAVE_TOKEN = 'gestao-efetivo:token';
    var CHAVE_USUARIO = 'gestao-efetivo:usuario';

    var estado = { token: null, usuario: null, arquivos: [] };

    var el = {
        telaLogin: document.getElementById('telaLogin'),
        telaApp: document.getElementById('telaApp'),
        formLogin: document.getElementById('formLogin'),
        login: document.getElementById('login'),
        senha: document.getElementById('senha'),
        btnEntrar: document.getElementById('btnEntrar'),
        erroLogin: document.getElementById('erroLogin'),
        nomeUsuario: document.getElementById('nomeUsuario'),
        iniciaisUsuario: document.getElementById('iniciaisUsuario'),
        btnSair: document.getElementById('btnSair'),
        dropzone: document.getElementById('dropzone'),
        inputArquivos: document.getElementById('arquivos'),
        listaArquivos: document.getElementById('listaArquivos'),
        unidadePadrao: document.getElementById('unidadePadrao'),
        situacaoPadrao: document.getElementById('situacaoPadrao'),
        atualizarExistentes: document.getElementById('atualizarExistentes'),
        btnSimular: document.getElementById('btnSimular'),
        btnImportar: document.getElementById('btnImportar'),
        btnLimpar: document.getElementById('btnLimpar'),
        btnModelo: document.getElementById('btnModelo'),
        erroEnvio: document.getElementById('erroEnvio'),
        resultado: document.getElementById('resultado')
    };

    // ---------- utilidades ----------

    function mostrar(no) { no.classList.remove('oculto'); }
    function esconder(no) { no.classList.add('oculto'); }

    function texto(valor) {
        return String(valor === null || valor === undefined ? '' : valor)
            .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
    }

    function tamanhoLegivel(bytes) {
        if (bytes < 1024) { return bytes + ' B'; }
        if (bytes < 1024 * 1024) { return (bytes / 1024).toFixed(0) + ' KB'; }
        return (bytes / 1024 / 1024).toFixed(1) + ' MB';
    }

    function iniciais(nome) {
        var partes = String(nome || '?').trim().split(/\s+/);
        return (partes[0].charAt(0) + (partes.length > 1 ? partes[partes.length - 1].charAt(0) : '')).toUpperCase();
    }

    function plural(n, singular, pluralForma) { return n + ' ' + (n === 1 ? singular : pluralForma); }

    function erro(no, mensagem) {
        no.textContent = mensagem;
        mostrar(no);
    }

    /** Chamada autenticada; um 401 significa token expirado, entao a tela volta ao login. */
    function api(caminho, opcoes) {
        opcoes = opcoes || {};
        opcoes.headers = opcoes.headers || {};
        if (estado.token) { opcoes.headers.Authorization = 'Bearer ' + estado.token; }
        return fetch(caminho, opcoes).then(function (resposta) {
            if (resposta.status === 401) {
                sair();
                throw new Error('Sua sessão expirou. Entre novamente.');
            }
            return resposta;
        });
    }

    function mensagemDeErro(resposta, corpo) {
        if (corpo && corpo.message) { return corpo.message; }
        if (resposta.status === 403) { return 'Seu perfil não tem permissão para importar efetivo.'; }
        return 'Não foi possível concluir (erro ' + resposta.status + ').';
    }

    // ---------- login ----------

    function entrar(evento) {
        evento.preventDefault();
        esconder(el.erroLogin);
        el.btnEntrar.disabled = true;
        el.btnEntrar.textContent = 'Entrando…';

        fetch('api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ login: el.login.value, senha: el.senha.value })
        }).then(function (resposta) {
            return resposta.json().catch(function () { return {}; }).then(function (corpo) {
                if (!resposta.ok) {
                    throw new Error(resposta.status === 401
                        ? 'Usuário ou senha inválidos.'
                        : mensagemDeErro(resposta, corpo));
                }
                return corpo;
            });
        }).then(function (corpo) {
            estado.token = corpo.token;
            estado.usuario = corpo.usuario || {};
            sessionStorage.setItem(CHAVE_TOKEN, estado.token);
            sessionStorage.setItem(CHAVE_USUARIO, JSON.stringify(estado.usuario));
            abrirApp();
        }).catch(function (e) {
            erro(el.erroLogin, e.message);
        }).finally(function () {
            el.btnEntrar.disabled = false;
            el.btnEntrar.textContent = 'Entrar';
        });
    }

    function sair() {
        estado.token = null;
        estado.usuario = null;
        sessionStorage.removeItem(CHAVE_TOKEN);
        sessionStorage.removeItem(CHAVE_USUARIO);
        esconder(el.telaApp);
        mostrar(el.telaLogin);
    }

    function abrirApp() {
        var nome = (estado.usuario && (estado.usuario.pessoaNome || estado.usuario.login)) || 'Usuário';
        el.nomeUsuario.textContent = nome;
        el.iniciaisUsuario.textContent = iniciais(nome);
        esconder(el.telaLogin);
        mostrar(el.telaApp);
        carregarCatalogos();
    }

    /** Preenche os selects com o que existe no banco — evita o usuario digitar uma unidade que nao existe. */
    function carregarCatalogos() {
        api('api/unidades').then(function (r) { return r.ok ? r.json() : []; }).then(function (unidades) {
            unidades.forEach(function (u) {
                var opcao = document.createElement('option');
                opcao.value = u.sigla;
                opcao.textContent = u.sigla + ' — ' + u.nome;
                el.unidadePadrao.appendChild(opcao);
            });
        }).catch(function () { /* o campo continua utilizavel: a planilha pode informar a unidade */ });

        api('api/situacoes').then(function (r) { return r.ok ? r.json() : []; }).then(function (situacoes) {
            situacoes.forEach(function (s) {
                var opcao = document.createElement('option');
                opcao.value = s.nome;
                opcao.textContent = s.nome;
                if (s.nome === 'Ativo') { opcao.selected = true; }
                el.situacaoPadrao.appendChild(opcao);
            });
        }).catch(function () { /* idem */ });
    }

    // ---------- arquivos ----------

    function adicionarArquivos(lista) {
        Array.prototype.forEach.call(lista, function (arquivo) {
            var repetido = estado.arquivos.some(function (a) {
                return a.name === arquivo.name && a.size === arquivo.size;
            });
            if (!repetido) { estado.arquivos.push(arquivo); }
        });
        desenharArquivos();
    }

    function desenharArquivos() {
        el.listaArquivos.innerHTML = '';
        estado.arquivos.forEach(function (arquivo, indice) {
            var item = document.createElement('li');
            item.innerHTML =
                '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#1f9d57" stroke-width="1.7"' +
                ' stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">' +
                '<path d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8z"></path>' +
                '<path d="M14 3v5h5"></path></svg>' +
                '<span class="arqnome">' + texto(arquivo.name) + '</span>' +
                '<span class="arqtam">' + tamanhoLegivel(arquivo.size) + '</span>' +
                '<button class="remover" type="button" aria-label="Remover ' + texto(arquivo.name) + '">&times;</button>';
            item.querySelector('.remover').addEventListener('click', function () {
                estado.arquivos.splice(indice, 1);
                desenharArquivos();
            });
            el.listaArquivos.appendChild(item);
        });
        atualizarBotoes();
    }

    function atualizarBotoes() {
        var temArquivo = estado.arquivos.length > 0;
        el.btnSimular.disabled = !temArquivo;
        el.btnImportar.disabled = !temArquivo;
        el.btnLimpar.classList.toggle('oculto', !temArquivo);
    }

    function limpar() {
        estado.arquivos = [];
        el.inputArquivos.value = '';
        desenharArquivos();
        esconder(el.resultado);
        esconder(el.erroEnvio);
    }

    // ---------- envio ----------

    function enviar(simulacao) {
        esconder(el.erroEnvio);
        esconder(el.resultado);

        var dados = new FormData();
        estado.arquivos.forEach(function (arquivo) { dados.append('arquivos', arquivo); });

        var parametros = new URLSearchParams();
        parametros.set('simulacao', String(simulacao));
        parametros.set('atualizarExistentes', String(el.atualizarExistentes.checked));
        if (el.unidadePadrao.value) { parametros.set('unidadePadrao', el.unidadePadrao.value); }
        if (el.situacaoPadrao.value) { parametros.set('situacaoPadrao', el.situacaoPadrao.value); }

        var botao = simulacao ? el.btnSimular : el.btnImportar;
        var rotulo = botao.textContent;
        el.btnSimular.disabled = true;
        el.btnImportar.disabled = true;
        botao.innerHTML = '<span class="carregando"></span>Processando…';

        api('api/pessoas/importacao?' + parametros.toString(), { method: 'POST', body: dados })
            .then(function (resposta) {
                return resposta.json().catch(function () { return {}; }).then(function (corpo) {
                    if (!resposta.ok) { throw new Error(mensagemDeErro(resposta, corpo)); }
                    return corpo;
                });
            })
            .then(desenharResultado)
            .catch(function (e) { erro(el.erroEnvio, e.message); })
            .finally(function () {
                botao.textContent = rotulo;
                atualizarBotoes();
            });
    }

    function baixarModelo() {
        el.btnModelo.disabled = true;
        api('api/pessoas/importacao/modelo')
            .then(function (resposta) {
                if (!resposta.ok) { throw new Error('Não consegui gerar o modelo (erro ' + resposta.status + ').'); }
                return resposta.blob();
            })
            .then(function (blob) {
                var url = URL.createObjectURL(blob);
                var link = document.createElement('a');
                link.href = url;
                link.download = 'modelo-importacao-efetivo.xlsx';
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
                URL.revokeObjectURL(url);
            })
            .catch(function (e) { erro(el.erroEnvio, e.message); })
            .finally(function () { el.btnModelo.disabled = false; });
    }

    // ---------- resultado ----------

    function desenharResultado(r) {
        var html = '';

        if (r.simulacao) {
            html += aviso('info', 'Isto foi uma conferência — nada foi gravado.',
                'Corrija o que estiver na lista de recusas e clique em “Importar de verdade”.');
        } else if (r.rejeitadas === 0) {
            html += aviso('ok', 'Importação concluída.',
                plural(r.criadas, 'pessoa cadastrada', 'pessoas cadastradas') + ' e ' +
                plural(r.atualizadas, 'atualizada', 'atualizadas') + '.');
        } else {
            html += aviso('ok', 'Importação concluída com recusas.',
                plural(r.criadas, 'pessoa entrou', 'pessoas entraram') + ' no sistema. ' +
                plural(r.rejeitadas, 'linha foi recusada', 'linhas foram recusadas') +
                ' — veja o motivo de cada uma abaixo.');
        }

        html += '<div class="statgrid">' +
            cartao('Linhas lidas', r.linhasLidas, '') +
            cartao('Cadastradas', r.criadas, 'ok') +
            cartao(r.atualizadas > 0 ? 'Atualizadas' : 'Ignoradas',
                r.atualizadas > 0 ? r.atualizadas : r.ignoradas, 'warn') +
            cartao('Recusadas', r.rejeitadas, r.rejeitadas > 0 ? 'bad' : '') +
            '</div>';

        (r.detalhes || []).forEach(function (arquivo) { html += blocoArquivo(arquivo); });
        el.resultado.innerHTML = html;
        mostrar(el.resultado);
        el.resultado.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }

    function aviso(tipo, titulo, corpo) {
        return '<div class="aviso aviso-' + tipo + '"><div><strong>' + texto(titulo) + '</strong>' +
            texto(corpo) + '</div></div>';
    }

    function cartao(rotulo, valor, classe) {
        return '<div class="card stat"><div class="statk">' + texto(rotulo) + '</div>' +
            '<div class="statv ' + classe + '">' + texto(valor) + '</div></div>';
    }

    function blocoArquivo(arquivo) {
        var html = '<div class="card" style="margin-bottom:16px"><div class="panel">';
        html += '<div class="arquivotitulo">' + texto(arquivo.arquivo);

        if (arquivo.erro) {
            html += '<span class="chip chip-bad">não foi lido</span></div>';
            html += '<div class="aviso aviso-erro" style="margin:0"><div>' + texto(arquivo.erro) + '</div></div>';
            return html + '</div></div>';
        }

        html += '<span class="chip chip-neutral">aba ' + texto(arquivo.aba) + '</span></div>';
        html += '<div class="resumolinha">' +
            plural(arquivo.linhasLidas, 'linha lida', 'linhas lidas') + ' · ' +
            arquivo.criadas + ' cadastrada(s) · ' + arquivo.atualizadas + ' atualizada(s) · ' +
            arquivo.ignoradas + ' ignorada(s) · ' + arquivo.rejeitadas + ' recusada(s)</div>';

        if (arquivo.colunasIgnoradas && arquivo.colunasIgnoradas.length) {
            html += '<div class="resumolinha">Colunas não aproveitadas: <strong>' +
                texto(arquivo.colunasIgnoradas.join(', ')) + '</strong></div>';
        }

        if (arquivo.linhasRejeitadas && arquivo.linhasRejeitadas.length) {
            html += '</div><table class="ptable"><thead><tr>' +
                '<th class="colnum">Linha</th><th>RE</th><th>Nome</th><th>Por que foi recusada</th>' +
                '</tr></thead><tbody>';
            arquivo.linhasRejeitadas.forEach(function (linha) {
                html += '<tr><td class="mono">' + texto(linha.linha) + '</td>' +
                    '<td class="mono">' + texto(linha.re || '—') + '</td>' +
                    '<td>' + texto(linha.nome || '—') + '</td>' +
                    '<td>' + texto(linha.motivo) + '</td></tr>';
            });
            return html + '</tbody></table></div>';
        }

        return html + '</div></div>';
    }

    // ---------- ligacoes ----------

    el.formLogin.addEventListener('submit', entrar);
    el.btnSair.addEventListener('click', sair);

    el.dropzone.addEventListener('click', function () { el.inputArquivos.click(); });
    el.dropzone.addEventListener('keydown', function (e) {
        if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); el.inputArquivos.click(); }
    });
    el.inputArquivos.addEventListener('change', function () { adicionarArquivos(el.inputArquivos.files); });

    ['dragenter', 'dragover'].forEach(function (evento) {
        el.dropzone.addEventListener(evento, function (e) {
            e.preventDefault();
            el.dropzone.classList.add('arrastando');
        });
    });
    ['dragleave', 'drop'].forEach(function (evento) {
        el.dropzone.addEventListener(evento, function (e) {
            e.preventDefault();
            el.dropzone.classList.remove('arrastando');
        });
    });
    el.dropzone.addEventListener('drop', function (e) {
        if (e.dataTransfer && e.dataTransfer.files) { adicionarArquivos(e.dataTransfer.files); }
    });

    el.btnSimular.addEventListener('click', function () { enviar(true); });
    el.btnImportar.addEventListener('click', function () { enviar(false); });
    el.btnLimpar.addEventListener('click', limpar);
    el.btnModelo.addEventListener('click', baixarModelo);

    // Recupera a sessao ao recarregar a pagina.
    var tokenSalvo = sessionStorage.getItem(CHAVE_TOKEN);
    if (tokenSalvo) {
        estado.token = tokenSalvo;
        try {
            estado.usuario = JSON.parse(sessionStorage.getItem(CHAVE_USUARIO) || '{}');
        } catch (e) {
            estado.usuario = {};
        }
        abrirApp();
    }
}());
