# Contexto do Projeto: Gestão de Bilheteira de Teatro

## 1. Visão Geral
Aplicação para gestão centralizada de salas de espetáculo, sessões e inventário de lugares.
O foco é evitar sobreposição de horários e garantir exclusividade de lugares por sessão.

## 2. Stack Técnica
- **Linguagem:** Java 21 (LTS)
- **Framework:** Spring Boot 3.4.x
- **Persistência:** Spring Data JPA / Hibernate
- **Base de Dados:** PostgreSQL (Docker container na porta 5433)
- **Gestão de Dependências:** Maven
- **Auxiliares:** Lombok (@Getter, @Setter)
- **Segurança:** BCrypt para passwords e RBAC (Role Based Access Control)

## 3. Entidades e Modelo de Domínio
- **Utilizador:** Admin, Operador, Cliente.
- **Evento:** Descrição, duração, classificação etária.
- **Sessão:** Liga Evento + Sala + Horário + Preço Base.
- **Sala/Zona/Lugar:** Composição física (Sala tem Zonas, Zonas têm Lugares).
- **Bilhete:** Ligação entre Lugar, Sessão e Utilizador.
- **Pagamento:** Regista a transação financeira de um ou mais bilhetes.

## 4. Regras de Negócio Críticas (IMPORTANTE)
- **Cálculo de Preço:** O preço final é calculado pela fórmula:
  $$PreçoFinal = (PreçoBaseSessao + TaxaZona) \times (1 - \%DescontoTipoBilhete)$$
- **Unicidade:** Impedir venda de mais de um Bilhete para a mesma combinação de [Sessão + Lugar].
- **Arquitetura:** Seguir o padrão Camadas:
    1. `Controller` (REST API)
    2. `Service/BLL` (Onde devem estar as validações)
    3. `Repository/DAL` (Interface JpaRepository)
- **DB Config:** `spring.jpa.hibernate.ddl-auto=none` (O esquema é gerido manualmente via SQL).

## 5. Requisitos de Implementação
- Os serviços devem validar regras antes de persistir (ex: verificar se a sala está livre antes de agendar sessão).
- Usar DTOs para comunicação nos controladores.
- Retornar erros descritivos (ex: "Lugar já ocupado").