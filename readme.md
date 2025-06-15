### Conhecimento em pratica
A ideia desse repositorio é para colocar em pratica os conhecimentos de Kotlin com Spring. Desta forma, o intuito é desenvolver um e-commerce utilizando uma estrutura de microserviços.

### Por que Microserviços?
A ideia principal é dividir um grande sistema monolítico em partes menores, independentes e focadas em uma única responsabilidade de negócio. Isso traz benefícios como:
*   **Manutenção Simplificada:** É mais fácil entender, modificar e corrigir um serviço pequeno e focado.
*   **Escalabilidade Independente:** Se o serviço de busca de produtos receber muito tráfego, você pode escalar apenas ele, sem precisar escalar todo o sistema.
*   **Autonomia de Times:** Em um cenário real, cada time poderia ser dono de um ou mais microserviços.
*   **Resiliência:** Se o serviço de notificações falhar, o cliente ainda pode navegar pelos produtos e fazer uma compra.

---

### Segregação dos Microserviços para um E-commerce

Aqui está uma proposta de divisão lógica, que é um ótimo ponto de partida. Cada item é um microserviço em potencial com suas responsabilidades bem definidas.

**1. Serviço de Usuários (User Service)**
*   **Responsabilidade:** Gerenciar tudo relacionado aos usuários.
*   **Funcionalidades:**
    *   Cadastro de novos clientes.
    *   Autenticação (Login) e geração de tokens (ex: JWT).
    *   Autorização (definição de papéis, como `CLIENTE` ou `ADMIN`).
    *   Gerenciamento de perfis de usuário (endereços, dados pessoais, etc.).

**2. Serviço de Produtos (Product Service)**
*   **Responsabilidade:** Gerenciar o catálogo de produtos.
*   **Funcionalidades:**
    *   CRUD (Criar, Ler, Atualizar, Deletar) de produtos.
    *   Gerenciamento de categorias e marcas.
    *   Busca e filtragem de produtos.
    *   Gerenciamento de avaliações (reviews) de produtos.

**3. Serviço de Carrinho de Compras (Shopping Cart Service)**
*   **Responsabilidade:** Gerenciar o carrinho de compras de cada usuário.
*   **Funcionalidades:**
    *   Adicionar/remover itens do carrinho.
    *   Atualizar a quantidade de itens.
    *   Calcular o subtotal.
    *   Limpar o carrinho.
*   **Observação:** Este serviço geralmente lida com dados temporários (voláteis). Por isso, é um ótimo candidato para usar um banco de dados em memória como o **Redis**, que é extremamente rápido.

**4. Serviço de Pedidos (Order Service)**
*   **Responsabilidade:** Orquestrar o processo de finalização da compra. É um serviço central que se comunica com vários outros.
*   **Funcionalidades:**
    *   Criar um novo pedido a partir do carrinho de compras.
    *   Coordenar com o Serviço de Pagamento.
    *   Coordenar com o Serviço de Estoque para reservar os produtos.
    *   Manter o histórico de pedidos do usuário.

**5. Serviço de Estoque (Inventory Service)**
*   **Responsabilidade:** Controlar a quantidade de cada produto disponível.
*   **Funcionalidades:**
    *   Verificar a disponibilidade de um produto.
    *   Decrementar o estoque quando um pedido é confirmado.
    *   Incrementar o estoque em caso de devolução ou reposição.
    *   Fornece uma API simples: `checkAvailability(productId, quantity)` e `updateStock(productId, quantity)`.

**6. Serviço de Pagamentos (Payment Service)**
*   **Responsabilidade:** Processar os pagamentos.
*   **Funcionalidades:**
    *   Integrar com gateways de pagamento externos (ex: Stripe, PagSeguro, Mercado Pago).
    *   Processar transações de cartão de crédito, boleto, Pix, etc.
    *   Notificar o Serviço de Pedidos sobre o sucesso ou falha do pagamento.

**7. Serviço de Notificações (Notification Service)**
*   **Responsabilidade:** Enviar comunicações para os usuários.
*   **Funcionalidades:**
    *   Enviar e-mail de confirmação de cadastro.
    *   Enviar e-mail de confirmação de pedido.
    *   Notificar sobre o status do envio.
    *   Pode ser estendido para enviar SMS ou notificações push.

---

### Componentes de Infraestrutura (Essenciais para a Arquitetura)

Além dos serviços de negócio, você precisará de alguns serviços de "suporte" para que tudo funcione de forma coesa.

*   **API Gateway (ex: Spring Cloud Gateway)**
    *   É o **ponto de entrada único** para todas as requisições externas (do navegador, do app mobile, etc.). Ele roteia cada requisição para o microserviço correto.
    *   **Funções:** Roteamento, segurança (centraliza a validação de tokens), balanceamento de carga, limitação de requisições (rate limiting).

*   **Service Registry (ex: Eureka, Consul)**
    *   É a "lista telefônica" dos seus microserviços. Cada serviço, ao iniciar, se registra no Service Registry informando seu nome e endereço (IP e porta).
    *   Quando o Serviço de Pedidos precisa falar com o Serviço de Estoque, ele pergunta ao Registry: "Onde está o Serviço de Estoque?". Isso permite que os serviços se encontrem dinamicamente.

*   **Config Server (ex: Spring Cloud Config)**
    *   Centraliza as configurações de todos os seus microserviços em um único lugar (geralmente um repositório Git).
    *   Evita que você tenha que copiar e colar configurações (como credenciais de banco de dados) em cada projeto. Ao iniciar, cada serviço busca suas configurações neste servidor.

---

### Como o Spring OpenFeign se encaixa?

O **Feign** é a "cola" que torna a comunicação entre seus serviços elegante e simples.

Imagine que o **Serviço de Pedidos** precisa verificar o preço de um produto que está no **Serviço de Produtos**.

*   **Sem Feign:** Você usaria uma ferramenta como `RestTemplate` ou `WebClient` para construir manualmente uma requisição HTTP (URL, headers, corpo) e tratar a resposta. É verboso e propenso a erros.
*   **Com Feign:** Você simplesmente cria uma interface em Kotlin no seu **Serviço de Pedidos**, assim:

    ```kotlin
    // Dentro do Order Service
    @FeignClient(name = "product-service") // O 'name' é o nome registrado no Eureka!
    interface ProductClient {
        @GetMapping("/api/products/{id}")
        fun getProductById(@PathVariable id: Long): ProductDetailsDTO
    }
    ```

    Depois, você pode simplesmente injetar `ProductClient` e chamar o método `getProductById()` como se fosse um método local. O Feign, em conjunto com o Service Registry, cuida de descobrir onde o `product-service` está, montar a requisição HTTP e converter a resposta para o objeto `ProductDetailsDTO`. É quase mágico!

---

### Roadmap de Desenvolvimento (Mapa do Projeto)

Siga estas fases para construir seu projeto de forma incremental e organizada.

**Fase 0: A Fundação da Arquitetura**
1.  **Configuração do Ambiente:** Crie um repositório Git. Defina a estrutura de pastas (sugestão: um monorepo com uma pasta para cada serviço).
2.  **Desenvolver o Service Registry (Eureka):** Este é o primeiro serviço a subir. É simples, mas fundamental.
3.  **Desenvolver o Config Server:** Crie o serviço e um repositório Git separado para as configurações.
4.  **Desenvolver o API Gateway:** Configure rotas iniciais para os serviços que virão.

**Fase 1: O Núcleo do E-commerce**
5.  **Desenvolver o Serviço de Usuários:** Crie as funcionalidades de cadastro e login. Configure a segurança com Spring Security.
6.  **Desenvolver o Serviço de Produtos:** Implemente o CRUD de produtos. Neste ponto, você já pode "povoar" seu e-commerce com dados.
7.  **Integração:** Faça o API Gateway proteger as rotas de produtos e redirecionar as de autenticação para o Serviço de Usuários.

**Fase 2: A Experiência de Compra**
8.  **Desenvolver o Serviço de Carrinho de Compras:** Use Redis para uma experiência rápida.
9.  **Desenvolver o Serviço de Estoque:** Crie a lógica para controlar a quantidade de produtos.
10. **Desenvolver o Serviço de Pedidos (Parte 1 - Orquestração):**
    *   Crie a lógica para transformar um carrinho em um pedido.
    *   Use o **Feign Client** para se comunicar com o Serviço de Produtos (para pegar detalhes e preços) e com o Serviço de Estoque (para verificar/reservar itens).

**Fase 3: Finalização e Pós-Venda**
11. **Desenvolver o Serviço de Pagamentos:** Integre com um gateway de pagamento (o modo de testes/sandbox do Stripe é ótimo para isso).
12. **Finalizar o Serviço de Pedidos (Parte 2 - Pagamento):** Adicione a chamada via Feign para o Serviço de Pagamentos e a lógica para confirmar o pedido e dar baixa no estoque.
13. **Desenvolver o Serviço de Notificações:** Crie um serviço simples para enviar e-mails (use o Spring Mail). Faça o Serviço de Usuários e o Serviço de Pedidos chamá-lo via Feign em eventos importantes (cadastro, pedido confirmado).

**Fase 4: Melhorias e Observabilidade**
14. **Logs e Rastreamento Distribuído:** Integre o **Spring Cloud Sleuth** e **Zipkin** para rastrear uma requisição através de múltiplos serviços. Isso é crucial para debugar problemas.
15. **Testes de Integração:** Escreva testes que simulem o fluxo completo: um usuário se cadastra, adiciona um produto ao carrinho e finaliza a compra.
16. **Containerização:** Crie um `Dockerfile` para cada serviço e um `docker-compose.yml` para orquestrar e rodar todo o ambiente localmente com um único comando.
