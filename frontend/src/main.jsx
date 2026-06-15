import React, { useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import './styles.css';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const emptyLogin = { email: 'admin@bank.local', password: 'admin123' };
const emptyRegister = { fullName: '', email: '', password: '' };
const emptyTransfer = { fromAccountId: '', toAccountNumber: '', amount: '', description: '' };
const emptyLoan = { principal: '', annualInterestRate: '', termMonths: '' };

function App() {
  const [token, setToken] = useState(() => localStorage.getItem('bankingToken') || '');
  const [authMode, setAuthMode] = useState('login');
  const [loginForm, setLoginForm] = useState(emptyLogin);
  const [registerForm, setRegisterForm] = useState(emptyRegister);
  const [accounts, setAccounts] = useState([]);
  const [selectedAccountId, setSelectedAccountId] = useState('');
  const [transactions, setTransactions] = useState([]);
  const [loans, setLoans] = useState([]);
  const [adminLoans, setAdminLoans] = useState([]);
  const [adminAccounts, setAdminAccounts] = useState([]);
  const [adminAccountDetails, setAdminAccountDetails] = useState(null);
  const [isAdmin, setIsAdmin] = useState(false);
  const [accountType, setAccountType] = useState('SAVINGS');
  const [moneyForm, setMoneyForm] = useState({ amount: '', description: '' });
  const [transferForm, setTransferForm] = useState(emptyTransfer);
  const [loanForm, setLoanForm] = useState(emptyLoan);
  const [notice, setNotice] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const selectedAccount = useMemo(
    () => accounts.find((account) => String(account.id) === String(selectedAccountId)),
    [accounts, selectedAccountId],
  );

  const isLoggedIn = Boolean(token);

  useEffect(() => {
    if (token) {
      localStorage.setItem('bankingToken', token);
      refreshDashboard();
    } else {
      localStorage.removeItem('bankingToken');
    }
  }, [token]);

  useEffect(() => {
    if (selectedAccountId && token) {
      loadTransactions(selectedAccountId);
    }
  }, [selectedAccountId]);

  async function request(path, options = {}) {
    const response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers,
      },
    });

    const text = await response.text();
    const data = text ? JSON.parse(text) : null;
    if (!response.ok) {
      throw new Error(data?.message || `Request failed with status ${response.status}`);
    }
    return data;
  }

  async function run(action, successMessage) {
    setLoading(true);
    setError('');
    setNotice('');
    try {
      const result = await action();
      if (successMessage) {
        setNotice(successMessage);
      }
      return result;
    } catch (err) {
      setError(err.message);
      return null;
    } finally {
      setLoading(false);
    }
  }

  async function submitAuth(event) {
    event.preventDefault();
    const payload = authMode === 'login' ? loginForm : registerForm;
    const path = authMode === 'login' ? '/api/auth/login' : '/api/auth/register';
    const result = await run(() => request(path, { method: 'POST', body: JSON.stringify(payload) }));
    if (result?.token) {
      setToken(result.token);
      setNotice(authMode === 'login' ? 'Logged in successfully.' : 'Account registered successfully.');
    }
  }

  async function refreshDashboard() {
    await run(async () => {
      const accountList = await request('/api/accounts');
      setAccounts(accountList);
      const nextSelected = selectedAccountId || accountList[0]?.id || '';
      setSelectedAccountId(nextSelected);
      if (nextSelected) {
        await loadTransactions(nextSelected);
      }
      const loanList = await request('/api/loans/mine');
      setLoans(loanList);
      try {
        setAdminLoans(await request('/api/loans'));
        setAdminAccounts(await request('/api/admin/accounts'));
        setIsAdmin(true);
      } catch {
        setAdminLoans([]);
        setAdminAccounts([]);
        setAdminAccountDetails(null);
        setIsAdmin(false);
      }
    });
  }

  async function loadTransactions(accountId) {
    const history = await request(`/api/accounts/${accountId}/transactions`);
    setTransactions(history);
  }

  async function createAccount(event) {
    event.preventDefault();
    await run(
      () => request('/api/accounts', { method: 'POST', body: JSON.stringify({ type: accountType }) }),
      'Account created.',
    );
    await refreshDashboard();
  }

  async function depositOrWithdraw(type) {
    if (!selectedAccountId) {
      setError('Create or select an account first.');
      return;
    }
    await run(
      () =>
        request(`/api/accounts/${selectedAccountId}/${type}`, {
          method: 'POST',
          body: JSON.stringify({ ...moneyForm, amount: Number(moneyForm.amount) }),
        }),
      type === 'deposit' ? 'Deposit completed.' : 'Withdrawal completed.',
    );
    setMoneyForm({ amount: '', description: '' });
    await refreshDashboard();
  }

  async function transfer(event) {
    event.preventDefault();
    const fromAccountId = transferForm.fromAccountId || selectedAccountId;
    await run(
      () =>
        request('/api/transfers', {
          method: 'POST',
          body: JSON.stringify({
            ...transferForm,
            fromAccountId: Number(fromAccountId),
            amount: Number(transferForm.amount),
          }),
        }),
      'Transfer completed.',
    );
    setTransferForm({ ...emptyTransfer, fromAccountId });
    await refreshDashboard();
  }

  async function applyLoan(event) {
    event.preventDefault();
    await run(
      () =>
        request('/api/loans', {
          method: 'POST',
          body: JSON.stringify({
            principal: Number(loanForm.principal),
            annualInterestRate: Number(loanForm.annualInterestRate),
            termMonths: Number(loanForm.termMonths),
          }),
        }),
      'Loan application submitted.',
    );
    setLoanForm(emptyLoan);
    await refreshDashboard();
  }

  async function decideLoan(loanId, status) {
    await run(
      () =>
        request(`/api/loans/${loanId}/decision`, {
          method: 'PATCH',
          body: JSON.stringify({ status }),
        }),
      `Loan ${status.toLowerCase()}.`,
    );
    await refreshDashboard();
  }

  async function loadAdminAccountDetails(accountId) {
    const details = await run(() => request(`/api/admin/accounts/${accountId}`));
    if (details) {
      setAdminAccountDetails(details);
    }
  }

  async function changeAccountFreeze(accountId, shouldFreeze) {
    const result = await run(
      () => request(`/api/admin/accounts/${accountId}/${shouldFreeze ? 'freeze' : 'unfreeze'}`, { method: 'PATCH' }),
      shouldFreeze ? 'Account frozen.' : 'Account reactivated.',
    );
    if (!result) {
      return;
    }
    const details = await request(`/api/admin/accounts/${accountId}`);
    setAdminAccountDetails(details);
    setAdminAccounts(await request('/api/admin/accounts'));
  }

  function logout() {
    setToken('');
    setAccounts([]);
    setTransactions([]);
    setLoans([]);
    setAdminLoans([]);
    setAdminAccounts([]);
    setAdminAccountDetails(null);
    setIsAdmin(false);
    setSelectedAccountId('');
    setNotice('Logged out.');
  }

  if (!isLoggedIn) {
    return (
      <main className="auth-shell">
        <section className="auth-panel">
          <div className="brand-mark">BS</div>
          <p className="eyebrow">Banking System</p>
          <h1>Secure banking operations dashboard</h1>
          <p className="subtle">
            Sign in with the seeded admin account or register a user to manage accounts, transfers, transactions, and loans.
          </p>

          <div className="segmented">
            <button className={authMode === 'login' ? 'active' : ''} onClick={() => setAuthMode('login')}>
              Login
            </button>
            <button className={authMode === 'register' ? 'active' : ''} onClick={() => setAuthMode('register')}>
              Register
            </button>
          </div>

          <form onSubmit={submitAuth} className="stack">
            {authMode === 'register' && (
              <label>
                Full name
                <input
                  value={registerForm.fullName}
                  onChange={(event) => setRegisterForm({ ...registerForm, fullName: event.target.value })}
                  placeholder="Asha Sharma"
                  required
                />
              </label>
            )}
            <label>
              Email
              <input
                type="email"
                value={authMode === 'login' ? loginForm.email : registerForm.email}
                onChange={(event) =>
                  authMode === 'login'
                    ? setLoginForm({ ...loginForm, email: event.target.value })
                    : setRegisterForm({ ...registerForm, email: event.target.value })
                }
                required
              />
            </label>
            <label>
              Password
              <input
                type="password"
                value={authMode === 'login' ? loginForm.password : registerForm.password}
                onChange={(event) =>
                  authMode === 'login'
                    ? setLoginForm({ ...loginForm, password: event.target.value })
                    : setRegisterForm({ ...registerForm, password: event.target.value })
                }
                required
                minLength={6}
              />
            </label>
            <button className="primary" disabled={loading}>
              {loading ? 'Please wait...' : authMode === 'login' ? 'Login' : 'Create account'}
            </button>
          </form>
          <Status notice={notice} error={error} />
        </section>
      </main>
    );
  }

  return (
    <main className="app-shell">
      <aside className="sidebar">
        <div>
          <div className="brand-row">
            <div className="brand-mark">BS</div>
            <div>
              <strong>Banking System</strong>
              <span>Spring Boot API</span>
            </div>
          </div>
          <nav>
            <a href="#accounts">Accounts</a>
            <a href="#transfers">Transfers</a>
            <a href="#transactions">Transactions</a>
            <a href="#loans">Loans</a>
            {isAdmin && <a href="#admin">Admin</a>}
          </nav>
        </div>
        <button className="ghost" onClick={logout}>Logout</button>
      </aside>

      <section className="workspace">
        <header className="topbar">
          <div>
            <p className="eyebrow">Dashboard</p>
            <h1>Banking operations</h1>
          </div>
          <button className="secondary" onClick={refreshDashboard} disabled={loading}>Refresh</button>
        </header>

        <Status notice={notice} error={error} />

        <section className="metrics">
          <Metric label="Accounts" value={accounts.length} />
          <Metric label="Total balance" value={formatMoney(accounts.reduce((sum, account) => sum + Number(account.balance), 0))} />
          <Metric label="Loans" value={loans.length} />
        </section>

        <section className="grid two" id="accounts">
          <div className="panel">
            <div className="panel-head">
              <h2>Accounts</h2>
              <form className="inline-form" onSubmit={createAccount}>
                <select value={accountType} onChange={(event) => setAccountType(event.target.value)}>
                  <option value="SAVINGS">Savings</option>
                  <option value="CURRENT">Current</option>
                </select>
                <button className="primary" disabled={loading}>Create</button>
              </form>
            </div>
            <div className="account-list">
              {accounts.map((account) => (
                <button
                  key={account.id}
                  className={`account-item ${String(selectedAccountId) === String(account.id) ? 'selected' : ''}`}
                  onClick={() => {
                    setSelectedAccountId(account.id);
                    setTransferForm({ ...transferForm, fromAccountId: account.id });
                  }}
                >
                  <span>
                    <strong>{account.accountNumber}</strong>
                    <small>{account.type} · {account.status}</small>
                  </span>
                  <b>{formatMoney(account.balance)}</b>
                </button>
              ))}
              {!accounts.length && <p className="empty">No accounts yet. Create one to start transactions.</p>}
            </div>
          </div>

          <div className="panel">
            <div className="panel-head">
              <h2>Cash operations</h2>
              <span className="pill">{selectedAccount?.accountNumber || 'No account selected'}</span>
            </div>
            <div className="stack">
              <label>
                Amount
                <input
                  type="number"
                  min="0.01"
                  step="0.01"
                  value={moneyForm.amount}
                  onChange={(event) => setMoneyForm({ ...moneyForm, amount: event.target.value })}
                  placeholder="5000"
                />
              </label>
              <label>
                Description
                <input
                  value={moneyForm.description}
                  onChange={(event) => setMoneyForm({ ...moneyForm, description: event.target.value })}
                  placeholder="Initial deposit"
                />
              </label>
              <div className="button-row">
                <button className="primary" onClick={() => depositOrWithdraw('deposit')} disabled={loading}>Deposit</button>
                <button className="secondary" onClick={() => depositOrWithdraw('withdraw')} disabled={loading}>Withdraw</button>
              </div>
            </div>
          </div>
        </section>

        <section className="grid two" id="transfers">
          <div className="panel">
            <div className="panel-head">
              <h2>Fund transfer</h2>
            </div>
            <form className="stack" onSubmit={transfer}>
              <label>
                From account
                <select
                  value={transferForm.fromAccountId || selectedAccountId}
                  onChange={(event) => setTransferForm({ ...transferForm, fromAccountId: event.target.value })}
                  required
                >
                  <option value="">Select account</option>
                  {accounts.map((account) => (
                    <option key={account.id} value={account.id}>{account.accountNumber}</option>
                  ))}
                </select>
              </label>
              <label>
                Destination account number
                <input
                  value={transferForm.toAccountNumber}
                  onChange={(event) => setTransferForm({ ...transferForm, toAccountNumber: event.target.value })}
                  placeholder="BA1234567890"
                  required
                />
              </label>
              <label>
                Amount
                <input
                  type="number"
                  min="0.01"
                  step="0.01"
                  value={transferForm.amount}
                  onChange={(event) => setTransferForm({ ...transferForm, amount: event.target.value })}
                  required
                />
              </label>
              <label>
                Description
                <input
                  value={transferForm.description}
                  onChange={(event) => setTransferForm({ ...transferForm, description: event.target.value })}
                  placeholder="Rent payment"
                />
              </label>
              <button className="primary" disabled={loading}>Send transfer</button>
            </form>
          </div>

          <div className="panel" id="transactions">
            <div className="panel-head">
              <h2>Transaction history</h2>
              <span className="pill">{transactions.length} records</span>
            </div>
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Type</th>
                    <th>Amount</th>
                    <th>Balance</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {transactions.map((transaction) => (
                    <tr key={transaction.id}>
                      <td>{transaction.type}</td>
                      <td>{formatMoney(transaction.amount)}</td>
                      <td>{formatMoney(transaction.balanceAfter)}</td>
                      <td>{transaction.status}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {!transactions.length && <p className="empty">Select an account to view its transactions.</p>}
            </div>
          </div>
        </section>

        <section className="grid two" id="loans">
          <div className="panel">
            <div className="panel-head">
              <h2>Loan application</h2>
            </div>
            <form className="stack" onSubmit={applyLoan}>
              <label>
                Principal
                <input
                  type="number"
                  min="1000"
                  value={loanForm.principal}
                  onChange={(event) => setLoanForm({ ...loanForm, principal: event.target.value })}
                  required
                />
              </label>
              <label>
                Interest rate %
                <input
                  type="number"
                  min="1"
                  step="0.01"
                  value={loanForm.annualInterestRate}
                  onChange={(event) => setLoanForm({ ...loanForm, annualInterestRate: event.target.value })}
                  required
                />
              </label>
              <label>
                Term months
                <input
                  type="number"
                  min="1"
                  value={loanForm.termMonths}
                  onChange={(event) => setLoanForm({ ...loanForm, termMonths: event.target.value })}
                  required
                />
              </label>
              <button className="primary" disabled={loading}>Apply</button>
            </form>
          </div>

          <div className="panel">
            <div className="panel-head">
              <h2>Loans</h2>
            </div>
            <div className="loan-list">
              {loans.map((loan) => <LoanItem key={loan.id} loan={loan} />)}
              {!loans.length && <p className="empty">No loan applications yet.</p>}
            </div>
          </div>
        </section>

        {isAdmin && (
          <section className="panel" id="admin">
            <div className="panel-head">
              <h2>Admin loan decisions</h2>
              <span className="pill">Admin</span>
            </div>
            <div className="loan-list">
              {adminLoans.map((loan) => (
                <LoanItem key={loan.id} loan={loan}>
                  {loan.status === 'PENDING' && (
                    <div className="button-row compact">
                      <button className="primary" onClick={() => decideLoan(loan.id, 'APPROVED')}>Approve</button>
                      <button className="danger" onClick={() => decideLoan(loan.id, 'REJECTED')}>Reject</button>
                    </div>
                  )}
                </LoanItem>
              ))}
              {!adminLoans.length && <p className="empty">No loan applications found.</p>}
            </div>
          </section>
        )}

        {isAdmin && (
          <section className="grid two">
            <div className="panel">
              <div className="panel-head">
                <h2>All bank accounts</h2>
                <span className="pill">{adminAccounts.length} accounts</span>
              </div>
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>Account</th>
                      <th>Owner</th>
                      <th>Role</th>
                      <th>Balance</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {adminAccounts.map((account) => (
                      <tr key={account.id}>
                        <td>
                          <button className="link-button" onClick={() => loadAdminAccountDetails(account.id)}>
                            {account.accountNumber}
                          </button>
                        </td>
                        <td>
                          <strong>{account.ownerName}</strong>
                          <small>{account.ownerEmail}</small>
                        </td>
                        <td>{account.ownerRoles.join(', ').replaceAll('ROLE_', '')}</td>
                        <td>{formatMoney(account.balance)}</td>
                        <td>
                          <span className={`status-chip ${account.status.toLowerCase()}`}>{account.status}</span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                {!adminAccounts.length && <p className="empty">No accounts found in the bank yet.</p>}
              </div>
            </div>

            <div className="panel">
              <div className="panel-head">
                <h2>Account inspection</h2>
                {adminAccountDetails && (
                  <span className={`status-chip ${adminAccountDetails.account.status.toLowerCase()}`}>
                    {adminAccountDetails.account.status}
                  </span>
                )}
              </div>
              {!adminAccountDetails && <p className="empty">Click an account number to inspect its balance, transactions, and running loans.</p>}
              {adminAccountDetails && (
                <div className="admin-detail">
                  <div className="detail-summary">
                    <div>
                      <span>Account number</span>
                      <strong>{adminAccountDetails.account.accountNumber}</strong>
                    </div>
                    <div>
                      <span>Balance</span>
                      <strong>{formatMoney(adminAccountDetails.account.balance)}</strong>
                    </div>
                    <div>
                      <span>Owner</span>
                      <strong>{adminAccountDetails.account.ownerName}</strong>
                    </div>
                    <div>
                      <span>Role</span>
                      <strong>{adminAccountDetails.account.ownerRoles.join(', ').replaceAll('ROLE_', '')}</strong>
                    </div>
                  </div>
                  <div className="button-row">
                    {adminAccountDetails.account.status === 'FROZEN' ? (
                      <button className="primary" onClick={() => changeAccountFreeze(adminAccountDetails.account.id, false)}>
                        Unfreeze account
                      </button>
                    ) : (
                      <button className="danger" onClick={() => changeAccountFreeze(adminAccountDetails.account.id, true)}>
                        Freeze account
                      </button>
                    )}
                  </div>

                  <h3>Running loans</h3>
                  <div className="loan-list compact-list">
                    {adminAccountDetails.runningLoans.map((loan) => <LoanItem key={loan.id} loan={loan} />)}
                    {!adminAccountDetails.runningLoans.length && <p className="empty">No approved running loans for this account owner.</p>}
                  </div>

                  <h3>Transaction history</h3>
                  <div className="table-wrap">
                    <table>
                      <thead>
                        <tr>
                          <th>Type</th>
                          <th>Amount</th>
                          <th>Balance</th>
                          <th>Status</th>
                        </tr>
                      </thead>
                      <tbody>
                        {adminAccountDetails.transactions.map((transaction) => (
                          <tr key={transaction.id}>
                            <td>{transaction.type}</td>
                            <td>{formatMoney(transaction.amount)}</td>
                            <td>{formatMoney(transaction.balanceAfter)}</td>
                            <td>{transaction.status}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                    {!adminAccountDetails.transactions.length && <p className="empty">No transactions for this account.</p>}
                  </div>
                </div>
              )}
            </div>
          </section>
        )}
      </section>
    </main>
  );
}

function Status({ notice, error }) {
  if (!notice && !error) {
    return null;
  }
  return <div className={`status ${error ? 'error' : 'success'}`}>{error || notice}</div>;
}

function Metric({ label, value }) {
  return (
    <div className="metric">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function LoanItem({ loan, children }) {
  return (
    <article className="loan-item">
      <div>
        <strong>{formatMoney(loan.principal)}</strong>
        <span>{loan.annualInterestRate}% · {loan.termMonths} months · EMI {formatMoney(loan.estimatedMonthlyPayment)}</span>
      </div>
      <div className="loan-actions">
        <span className={`status-chip ${loan.status.toLowerCase()}`}>{loan.status}</span>
        {children}
      </div>
    </article>
  );
}

function formatMoney(value) {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 2,
  }).format(Number(value || 0));
}

createRoot(document.getElementById('root')).render(<App />);
