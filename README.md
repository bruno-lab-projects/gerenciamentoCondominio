# 🏢 Sistema de Gestão de Condomínio

Sistema completo para **gerenciamento financeiro e administrativo de condomínios**, com foco em **transparência fiscal**, **automação de documentos** e **controle rigoroso de despesas**.

> 🎓 **Contexto Acadêmico**
> Software desenvolvido e **aprovado como Projeto de Extensão Universitária (PUCPR)**, aplicando arquitetura Web segura, geração dinâmica de documentos e solução de problemas reais da gestão condominial.

---

## 🧰 Tecnologias Utilizadas

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge\&logo=openjdk\&logoColor=white)
![Spring Boot](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge\&logo=spring\&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge\&logo=postgresql\&logoColor=white)
![Bootstrap](https://img.shields.io/badge/bootstrap-%238511FA.svg?style=for-the-badge\&logo=bootstrap\&logoColor=white)

---

## 📋 Sobre o Projeto

O objetivo principal do sistema é **profissionalizar a gestão financeira do condomínio**, substituindo planilhas manuais por um sistema **auditável, seguro e centralizado**.

A aplicação automatiza:

* Cálculo de rateios
* Controle de despesas fixas e variáveis
* Geração de documentos legais (recibos e balancetes)

---

## ⚙️ Funcionalidades

### 📊 Inteligência Financeira

* **Despesas Recorrentes Automatizadas**
  Custos fixos essenciais (Água, Energia, Elevador, Funcionários) são priorizados e controlados automaticamente.

* **Gestão de Despesas Variáveis**
  Cadastro flexível de gastos esporádicos, como manutenções e serviços extraordinários.

* **Relatórios de Prestação de Contas**
  Geração automática de balancetes mensais com cálculo de **Receitas × Despesas = Saldo**, filtrável por mês e ano.

---

### 📄 Motor de Geração de Documentos (PDF)

* **Recibos em Lote**
  Emissão automática de recibos individualizados para todas as unidades com apenas um clique.

* **Segurança Jurídica**
  Cada recibo contém **duas vias** (Via Síndica e Via Morador) na mesma página.

* **Personalização**
  Nome da administração e assinatura digital configuráveis diretamente no documento.

---

### 🔐 Segurança e Controle de Acesso

Implementação completa com **Spring Security**:

* **Perfil Administrador (Síndica)**
  Acesso total ao sistema, lançamentos financeiros, auditoria e relatórios.

* **Perfil Morador**
  Acesso restrito para visualização da transparência financeira e download de recibos.

* **Tratamento de Erros**
  Páginas personalizadas e amigáveis para erros **403 (Acesso Negado)** e **404 (Página Não Encontrada)**.

---

## 🏗️ Arquitetura e Stack Técnica

* **Backend:** Java 17 + Spring Boot 3.4.0
* **Frontend:** Thymeleaf (Server-Side Rendering) + Bootstrap 5 (Mobile First)
* **Banco de Dados:** PostgreSQL
* **Segurança:** Spring Security (Autenticação e Autorização por Roles)
* **Relatórios / PDF:** Flying Saucer (HTML → PDF com iText)
* **Build Tool:** Maven

---

## 🖼️ Telas do Sistema

### 🔐 Tela de Login

<img src="https://github.com/user-attachments/assets/20d5bb73-faa8-4ffe-af8f-ee4e8dc546d9" width="400" />

---

### 🏠 Tela Inicial (Dashboard)

<img src="https://github.com/user-attachments/assets/4e7a73a9-7501-4e71-9e6c-3f7001331f66" width="800" />

---

### 💸 Gestão de Despesas

<img src="https://github.com/user-attachments/assets/7633634b-8779-4515-a68c-79cacaeda798" width="800" />

---

### 🔁 Despesas Recorrentes

<img src="https://github.com/user-attachments/assets/26f56625-7b13-494c-a634-85037d4d8ff6" width="800" />

---

## 🚀 Como Executar o Projeto

### ✅ Pré-requisitos

* Java 21 ou superior
* Maven instalado
* Docker e Docker Compose (para o PostgreSQL)

---

### 📦 Instalação Rápida

1. **Clone o repositório:**

```bash
git clone https://github.com/bruno-lab-projects/gerenciamentoCondominio.git
cd gerenciamentoCondominio
```

2. **Configure as credenciais:**

```bash
# Copie o arquivo de exemplo
cp .env.example .env

# Edite com suas credenciais
nano .env
```

**Configuração mínima do `.env`:**
```env
ADMIN_EMAIL=sindica@seucondominio.com
ADMIN_PASSWORD=SuaSenhaSegura@2026
DB_PASSWORD=senhaDoPostgres
```

📖 **Para configuração detalhada, veja:** [CREDENCIAIS.md](CREDENCIAIS.md)

3. **Inicie o banco de dados:**

```bash
docker-compose up -d
```

4. **Execute a aplicação:**

```bash
./mvnw spring-boot:run
```

5. **Acesse no navegador:**

```
http://localhost:8082
```

**Login padrão:**
- Email: `sindica@predio.com` (ou o configurado no `.env`)
- Senha: `123456` (ou a configurada no `.env`)

---

### 🔒 Segurança

⚠️ **IMPORTANTE:** Este projeto usa variáveis de ambiente para credenciais sensíveis.

- ✅ Arquivo `.env` **NÃO** é commitado no Git
- ✅ Use `.env.example` como template
- ✅ Senhas são criptografadas com BCrypt
- ✅ Configuração diferente para dev/produção

📖 **Guia completo:** [CREDENCIAIS.md](CREDENCIAIS.md)

---

## 👨‍💻 Autor

[**Bruno Barreto**](github.com/brunombs)
