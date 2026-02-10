#!/usr/bin/env python3
"""
Database Seeding Script - ERP Kế toán Thông tư 99/2025/TT-BTC
Seed dữ liệu mẫu cho testing và UAT
"""

import csv
import os
import sys
from datetime import date, datetime
from decimal import Decimal
from pathlib import Path
from uuid import uuid4


def read_csv(filepath: str) -> list[dict]:
    """Đọc file CSV."""
    if not os.path.exists(filepath):
        return []
    data = []
    with open(filepath, "r", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            data.append(row)
    return data


def main():
    """Main function."""
    print("=" * 60)
    print("Database Seeding - ERP Kế toán Thông tư 99/2025/TT-BTC")
    print("=" * 60)

    # Initialize database first
    from app.infrastructure.database import init_db

    init_db()

    from sqlalchemy.orm import Session
    from app.infrastructure.database import SessionLocal
    from app.infrastructure.database.models import (
        Company,
        Account,
        Customer,
        Supplier,
        Product,
        Contract,
        Employee,
        JournalEntry,
        JournalEntryLine,
        FiscalPeriod,
        TaxConfig,
        ExchangeRateHistory,
        AccountBalance,
    )

    db = SessionLocal()

    try:
        # Seed company
        company = db.query(Company).filter(Company.tax_code == "0101234567").first()
        if not company:
            company = Company(
                id=uuid4(),
                tax_code="0101234567",
                name="Công ty TNHH Thương mại Dịch vụ Demo Việt",
                address="123 Đường ABC, Quận 1, TP. Hồ Chí Minh",
                phone="028 1234 5678",
                email="demo@demo.vn",
                representative_name="Nguyễn Văn Demo",
                fiscal_year_start=1,
                is_active=True,
            )
            db.add(company)
            db.commit()
            print(f"✓ Created company: {company.name}")
        else:
            print(f"✓ Company already exists: {company.name}")

        company_id = company.id

        # Seed accounts
        accounts_file = Path(__file__).parent / "seed_data" / "01_chart_of_accounts.csv"
        accounts_data = read_csv(str(accounts_file))
        print(f"\n📦 Seeding {len(accounts_data)} accounts...")

        for row in accounts_data:
            exists = (
                db.query(Account)
                .filter(Account.company_id == company_id, Account.code == row["account_code"])
                .first()
            )

            if not exists:
                account = Account(
                    id=uuid4(),
                    company_id=company_id,
                    code=row["account_code"],
                    name=row["account_name"],
                    account_type=row.get("account_type", "ASSET"),
                    parent_code=row.get("parent_code"),
                    is_detail=row.get("is_detail", "True").lower() == "true",
                    balance_direction=row.get("balance_direction", "DEBIT"),
                    is_system=True,
                    is_active=True,
                )
                db.add(account)
        db.commit()
        print(f"✓ Seeded {len(accounts_data)} accounts")

        # Seed customers
        customers_file = Path(__file__).parent / "seed_data" / "02_customers.csv"
        customers_data = read_csv(str(customers_file))
        print(f"\n📦 Seeding {len(customers_data)} customers...")

        for row in customers_data:
            exists = (
                db.query(Customer)
                .filter(
                    Customer.company_id == company_id,
                    Customer.customer_code == row["customer_code"],
                )
                .first()
            )

            if not exists:
                customer = Customer(
                    id=uuid4(),
                    company_id=company_id,
                    customer_code=row["customer_code"],
                    customer_name=row["customer_name"],
                    customer_type=row.get("customer_type", "BUSINESS"),
                    tax_code=row.get("tax_code"),
                    address=row.get("address", ""),
                    phone=row.get("phone", ""),
                    email=row.get("email", ""),
                    channel=row.get("channel", "ONSITE"),
                    online_platform=row.get("online_platform", ""),
                    credit_limit=Decimal(row.get("credit_limit", "0")),
                    credit_term_days=int(row.get("credit_term_days", 0)),
                    is_active=True,
                )
                db.add(customer)
        db.commit()
        print(f"✓ Seeded {len(customers_data)} customers")

        # Seed suppliers
        suppliers_file = Path(__file__).parent / "seed_data" / "03_suppliers.csv"
        suppliers_data = read_csv(str(suppliers_file))
        print(f"\n📦 Seeding {len(suppliers_data)} suppliers...")

        for row in suppliers_data:
            exists = (
                db.query(Supplier)
                .filter(
                    Supplier.company_id == company_id,
                    Supplier.supplier_code == row["supplier_code"],
                )
                .first()
            )

            if not exists:
                supplier = Supplier(
                    id=uuid4(),
                    company_id=company_id,
                    supplier_code=row["supplier_code"],
                    supplier_name=row["supplier_name"],
                    supplier_type=row.get("supplier_type", "BUSINESS"),
                    tax_code=row.get("tax_code"),
                    address=row.get("address", ""),
                    phone=row.get("phone", ""),
                    email=row.get("email", ""),
                    account_code=row.get("account_code", "3311"),
                    credit_limit=Decimal(row.get("credit_limit", "0")),
                    credit_term_days=int(row.get("credit_term_days", 0)),
                    is_active=True,
                )
                db.add(supplier)
        db.commit()
        print(f"✓ Seeded {len(suppliers_data)} suppliers")

        # Seed products
        products_file = Path(__file__).parent / "seed_data" / "04_products.csv"
        products_data = read_csv(str(products_file))
        print(f"\n📦 Seeding {len(products_data)} products...")

        for row in products_data:
            exists = (
                db.query(Product)
                .filter(
                    Product.company_id == company_id, Product.product_code == row["product_code"]
                )
                .first()
            )

            if not exists:
                product = Product(
                    id=uuid4(),
                    company_id=company_id,
                    product_code=row["product_code"],
                    product_name=row["product_name"],
                    product_type=row.get("product_type", "GOODS"),
                    unit=row.get("unit", "chiếc"),
                    unit_price=Decimal(row.get("unit_price", "0")),
                    purchase_price=Decimal(row.get("purchase_price", "0")),
                    vat_rate=Decimal(row.get("vat_rate", "10")),
                    inventory_account=row.get("inventory_account", "1561"),
                    cost_account=row.get("cost_account", "632"),
                    stock_account=row.get("stock_account", "1561"),
                    category=row.get("category", ""),
                    brand=row.get("brand", ""),
                    origin=row.get("origin", ""),
                    barcode=row.get("barcode", ""),
                    min_stock=int(row.get("min_stock", 0)),
                    max_stock=int(row.get("max_stock", 0)),
                    lead_time_days=int(row.get("lead_time_days", 0)),
                    is_active=True,
                )
                db.add(product)
        db.commit()
        print(f"✓ Seeded {len(products_data)} products")

        # Seed contracts
        contracts_file = Path(__file__).parent / "seed_data" / "05_contracts.csv"
        contracts_data = read_csv(str(contracts_file))
        print(f"\n📦 Seeding {len(contracts_data)} contracts...")

        for row in contracts_data:
            exists = (
                db.query(Contract)
                .filter(
                    Contract.company_id == company_id,
                    Contract.contract_code == row["contract_code"],
                )
                .first()
            )

            if not exists:
                try:
                    start_date = (
                        datetime.strptime(row.get("start_date", "01/01/2026"), "%d/%m/%Y").date()
                        if row.get("start_date")
                        else date(2026, 1, 1)
                    )
                    end_date = (
                        datetime.strptime(row.get("end_date", "31/12/2026"), "%d/%m/%Y").date()
                        if row.get("end_date")
                        else date(2026, 12, 31)
                    )
                except:
                    start_date = date(2026, 1, 1)
                    end_date = date(2026, 12, 31)

                contract = Contract(
                    id=uuid4(),
                    company_id=company_id,
                    contract_code=row["contract_code"],
                    contract_type=row.get("contract_type", "CONSULTING"),
                    customer_code=row.get("customer_code"),
                    customer_name=row.get("customer_name"),
                    start_date=start_date,
                    end_date=end_date,
                    total_value=Decimal(row.get("total_value", "0")),
                    vat_rate=Decimal(row.get("vat_rate", "10")),
                    revenue_recognition=row.get("revenue_recognition", "PERCENTAGE"),
                    progress_payment=row.get("progress_payment", "MONTHLY"),
                    service_description=row.get("service_description", ""),
                    account_code=row.get("account_code", "5112"),
                    status="ACTIVE",
                    is_active=True,
                )
                db.add(contract)
        db.commit()
        print(f"✓ Seeded {len(contracts_data)} contracts")

        # Seed employees
        employees_file = Path(__file__).parent / "seed_data" / "06_employees.csv"
        employees_data = read_csv(str(employees_file))
        print(f"\n📦 Seeding {len(employees_data)} employees...")

        for row in employees_data:
            exists = (
                db.query(Employee)
                .filter(
                    Employee.company_id == company_id,
                    Employee.employee_code == row["employee_code"],
                )
                .first()
            )

            if not exists:
                try:
                    hire_date = (
                        datetime.strptime(row.get("hire_date", "01/01/2026"), "%d/%m/%Y").date()
                        if row.get("hire_date")
                        else date(2026, 1, 1)
                    )
                except:
                    hire_date = date(2026, 1, 1)

                employee = Employee(
                    id=uuid4(),
                    company_id=company_id,
                    employee_code=row["employee_code"],
                    employee_name=row["employee_name"],
                    department=row.get("department", ""),
                    position=row.get("position", ""),
                    hire_date=hire_date,
                    salary=Decimal(row.get("salary", "0")),
                    allowance=Decimal(row.get("allowance", "0")),
                    tax_deduction=Decimal(row.get("tax_deduction", "11000000")),
                    insurance_rate=Decimal(row.get("insurance_rate", "0.08")),
                    tax_rate=Decimal(row.get("tax_rate", "0.1")),
                    payment_method=row.get("payment_method", "BANK"),
                    bank_account=row.get("bank_account", ""),
                    id_card=row.get("id_card", ""),
                    address=row.get("address", ""),
                    phone=row.get("phone", ""),
                    email=row.get("email", ""),
                    note=row.get("note", ""),
                    is_active=True,
                )
                db.add(employee)
        db.commit()
        print(f"✓ Seeded {len(employees_data)} employees")

        # Seed opening balances
        balances_file = Path(__file__).parent / "seed_data" / "14_opening_balances.csv"
        balances_data = read_csv(str(balances_file))
        print(f"\n📦 Seeding {len(balances_data)} opening balances...")

        for row in balances_data:
            account = (
                db.query(Account)
                .filter(Account.company_id == company_id, Account.code == row["account_code"])
                .first()
            )

            if account:
                debit = Decimal(row.get("debit_balance", "0")) if row.get("debit_balance") else None
                credit = (
                    Decimal(row.get("credit_balance", "0")) if row.get("credit_balance") else None
                )

                exists = (
                    db.query(AccountBalance)
                    .filter(
                        AccountBalance.company_id == company_id,
                        AccountBalance.account_code == row["account_code"],
                        AccountBalance.year == int(row.get("year", 2026)),
                        AccountBalance.period_value == int(row.get("period_value", 1)),
                    )
                    .first()
                )

                if not exists:
                    balance = AccountBalance(
                        id=uuid4(),
                        company_id=company_id,
                        account_id=account.id,
                        account_code=row["account_code"],
                        period_type=row.get("period_type", "MONTH"),
                        year=int(row.get("year", 2026)),
                        period_value=int(row.get("period_value", 1)),
                        start_date=date(2026, 1, 1),
                        end_date=date(2026, 1, 31),
                        opening_debit=debit,
                        opening_credit=credit,
                        period_debit=Decimal("0"),
                        period_credit=Decimal("0"),
                        closing_debit=debit,
                        closing_credit=credit,
                    )
                    db.add(balance)
        db.commit()
        print(f"✓ Seeded {len(balances_data)} opening balances")

        # Seed exchange rates
        rates_file = Path(__file__).parent / "seed_data" / "15_exchange_rates.csv"
        rates_data = read_csv(str(rates_file))
        print(f"\n📦 Seeding {len(rates_data)} exchange rates...")

        for row in rates_data:
            try:
                valuation_date = (
                    datetime.strptime(row.get("rate_date", "01/01/2026"), "%d/%m/%Y").date()
                    if row.get("rate_date")
                    else date(2026, 1, 1)
                )
            except:
                valuation_date = date(2026, 1, 1)

            exists = (
                db.query(ExchangeRateHistory)
                .filter(
                    ExchangeRateHistory.currency == row["currency"],
                    ExchangeRateHistory.valuation_date == valuation_date,
                )
                .first()
            )

            if not exists:
                rate = ExchangeRateHistory(
                    id=uuid4(),
                    currency=row["currency"],
                    valuation_date=valuation_date,
                    rate_type=row.get("rate_type", "REALTIME"),
                    rate=Decimal(row.get("mid_rate", "0")),
                    source=row.get("source", "VCB"),
                )
                db.add(rate)
        db.commit()
        print(f"✓ Seeded {len(rates_data)} exchange rates")

        # Validate
        print("\n=== Validating Seed Data ===")

        # Check balance
        balances = (
            db.query(AccountBalance)
            .filter(
                AccountBalance.company_id == company_id,
                AccountBalance.year == 2026,
                AccountBalance.period_value == 1,
            )
            .all()
        )

        total_debit = sum(b.closing_debit or Decimal("0") for b in balances)
        total_credit = sum(b.closing_credit or Decimal("0") for b in balances)
        diff = abs(total_debit - total_credit)

        if diff > Decimal("1"):
            print(f"⚠️ Cân đối tài khoản không khớp: Debit={total_debit}, Credit={total_credit}")
        else:
            print(f"✓ Cân đối tài khoản OK: Debit={total_debit}, Credit={total_credit}")

        print("\n" + "=" * 60)
        print("Seeding completed successfully!")
        print(f"Company ID: {company_id}")
        print("=" * 60)

    except Exception as e:
        print(f"\n❌ Error: {e}")
        db.rollback()
        raise
    finally:
        db.close()


if __name__ == "__main__":
    main()
