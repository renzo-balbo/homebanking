const { createApp } = Vue

createApp({
  data() {
    return {
      client: {},
      accounts: [],
      clientLoans: [],
      cards: [],
      newCardType: "",
      newCardColor: "",
      cardToDelete: "",
      transactionAmount: 0,
      originAccount: "",
      destinyAccount: "",
      transactionDescription: "",
      destinyAccountSelector: "",
      loans: [],
      selectedLoan: 0,
      loanAmount: "",
      selectedLoanPayments: "",
      loanDestinyAccount: "",
      actualDate: "",
      newAccountType: "",
      accountToDisable: "",

    }
  },

  created() {
    this.actualDate = new Date().toISOString()
    this.loadClientData()

  },

  mounted() { },

  methods: {

    loadClientData() {
      axios.get("/api/clients/current")
        .then(response => {
          this.client = response.data
          this.accounts = this.client.accounts.sort((a, b) => a.id - b.id).filter(account => account.active == true)
          this.accounts.forEach(account => {
            account.balance = this.moneyFormatter(account.balance)
          })
          this.clientLoans = this.client.loans.sort((a, b) => b.id - a.id)
          this.cards = this.client.cards.filter(card => card.active == true).sort((a, b) => a.id - b.id)
          this.loadLoansData()
          this.normalizeCardsDate(this.cards)
        })
    },

    loadLoansData() {
      axios.get("/api/loans")
        .then(response => {
          this.loans = response.data.sort((a, b) => a.id - b.id)
          this.loans.forEach(loan => {
            loan.maxAmount = this.moneyFormatter(loan.maxAmount)
          })
        })
    },

    normalizeCardsDate(cardsArray) {
      cardsArray.forEach(card => {
        card.thruDate = card.thruDate.slice(2, 7)
      })

    },

    createNewAccount() {
      axios.post("/api/clients/current/accounts", `clientEmail=${this.client.email}`, { headers: { 'content-type': 'application/x-www-form-urlencoded' } })
        .then(() => {
          window.location.reload()
        })
        .catch(error => swal(error.response.data, {
          dangerMode: true
        }))
    },

    createNewCard() {
      axios.post("/api/clients/current/cards", `cardColor=${this.newCardColor}&cardType=${this.newCardType}`, { headers: { 'content-type': 'application/x-www-form-urlencoded' } })
        .then(() => window.location.href = "./cards.html")
        .catch(error => swal(error.response.data, {
          dangerMode: true
        }))
    },

    deleteCard() {
      swal("Are you sure you want to delete this?", {
        dangerMode: true,
        buttons: true,
      })
        .then(response => {
          if (response) {
            axios.patch("/api/clients/current/cards", `cardNumber=${this.cardToDelete}`, { headers: { 'content-type': 'application/x-www-form-urlencoded' } })
              .then(() => {
                swal("Card disabled successfully!", {
                  icon: "success",
                  dangerMode: true
                })
                setTimeout(function () { window.location.href = "./cards.html" }, 2500)
              })
          }
        })
    },

    transferMoney() {
      swal("Are you sure?", {
        dangerMode: true,
        buttons: true,
      })
        .then(response => {
          if (response) {
            axios.post("/api/transactions", `amount=${this.transactionAmount}&description=${this.transactionDescription}&originAccountNumber=${this.originAccount}&destinyAccountNumber=${this.destinyAccount}`, { headers: { 'content-type': 'application/x-www-form-urlencoded' } })
              .then(() => window.location.href = `./accounts.html`)
              .catch(error => swal(error.response.data, {
                dangerMode: true
              }))
          }
        })
    },

    confirmLoan() {
      swal("Are you sure you want to request this loan? Doing so you confirm that you are aware of the interest rates applied.", {
        dangerMode: true,
        buttons: true,
      })
        .then(response => {
          if (response) {
            this.createLoan()
          }
        })
        .catch(error => console.log(error))


    },

    createLoan() {
      axios.post("/api/loans", { id: `${this.selectedLoan}`, amount: `${this.loanAmount}`, payments: `${this.selectedLoanPayments}`, destinyAccountNumber: `${this.loanDestinyAccount}` })
        .then(() => {
          swal("Loan requested successfully!", {
            icon: "success",
            dangerMode: true
          })
          setTimeout(function () { window.location.href = "./accounts.html" }, 2500)
        }
        )
        .catch(error => swal(error.response.data, {
          dangerMode: true
        }))

    },

    createNewAccount() {
      axios.post('/api/clients/current/accounts', `accountType=${this.newAccountType}`)
        .then(() => {
          swal("New account created successfully!", {
            icon: "success",
            dangerMode: true
          })
          setTimeout(function () { window.location.href = "./accounts.html" }, 2000)
        }
        )
        .catch(error => swal(error.response.data, {
          dangerMode: true
        }))
    },

    confirmationToDisableAnAccount() {
      swal("Are you sure you want to disable this account? This action can not be reversed.", {
        dangerMode: true,
        buttons: true,
      })
        .then(response => {
          if (response) {
            this.disableAccount()
          }
        })
        .catch(error => console.log(error))


    },

    disableAccount() {
      axios.patch('/api/clients/current/accounts', `accountNumber=${this.accountToDisable}`)
        .then(() => {
          swal("Account disabled successfully!", {
            icon: "success",
            dangerMode: true
          })
          setTimeout(function () { window.location.href = "./accounts.html" }, 2000)
        })
        .catch(error => swal(error.response.data, {
          dangerMode: true
        }))
    },

    logout() {
      axios.post('/api/logout')
        .then(response => window.location.href = "./index.html")
    },

    moneyFormatter(numberToFormat) {
      let formatter = new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD',
      })
      return formatter.format(numberToFormat)
    },




  },
  computed: {

    loanInterest() {
      let amountToPay = this.loanAmount
      let monthlyFee = 0
      switch (this.selectedLoan) {
        case 1://Personal loan
          switch (this.selectedLoanPayments) {
            case 6:
              amountToPay = amountToPay * 1.20
              break;
            case 12:
              amountToPay = amountToPay * 1.22
              break;
            case 24:
              amountToPay = amountToPay * 1.25
              break;
            case 36:
              amountToPay = amountToPay * 1.30
              break;
            default:
              break;
          }
          break;
        case 2: //Automobile Loan
          switch (this.selectedLoanPayments) {
            case 6:
              amountToPay = amountToPay * 1.20
              break;
            case 12:
              amountToPay = amountToPay * 1.23
              break;
            case 24:
              amountToPay = amountToPay * 1.27
              break;
            case 36:
              amountToPay = amountToPay * 1.32
              break;
            default:
              break;
          }
          break;
        case 3://Mortgage Loan
          switch (this.selectedLoanPayments) {
            case 12:
              amountToPay = amountToPay * 1.20
              break;
            case 24:
              amountToPay = amountToPay * 1.25
              break;
            case 36:
              amountToPay = amountToPay * 1.30
              break;
            case 48:
              amountToPay = amountToPay * 1.35
              break;
            case 60:
              amountToPay = amountToPay * 1.40
            default:
              break;
          }
          break;
        default:
          break;
      }
      monthlyFee = amountToPay / this.selectedLoanPayments
      return this.moneyFormatter(amountToPay) + " in " + this.selectedLoanPayments + " fees of " + this.moneyFormatter(monthlyFee)
    },


  },

}).mount('#app')