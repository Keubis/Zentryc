# Zentryc 

Aplicación Android de gestión de gastos personales desarrollada en Kotlin.

## Funcionalidades
- Registro de ingresos y gastos con descripción, importe, fecha y categoría
- Gestión de categorías personalizadas con color identificativo
- Dashboard con balance mensual y últimos movimientos
- Selector de mes con navegación entre meses
- Lista de movimientos con filtro por fecha y por categoría (combinables)
- Selección múltiple de movimientos para eliminación en lote
- Edición de movimientos mediante pulsación larga
- Estadísticas mensuales con gráfica de tarta y desglose por categoría
- Autenticación de usuarios con Firebase Authentication
- Sincronización en la nube con Firebase Firestore
- Arquitectura offline-first: funciona sin conexión

## Tecnologías
- Kotlin 2.0.21 + Android Studio Otter
- Room 2.7.0 (SQLite local)
- MVVM (ViewModel + LiveData)
- Firebase Authentication + Firestore
- Navigation Component (Single Activity)
- MPAndroidChart v3.1.0
- KSP 2.0.21-1.0.28
- Material Design 3
- Git / GitHub

## Estructura del proyecto
- `data/model/` — Modelos de datos (Expense, Category, TransactionWithCategory)
- `data/database/` — Base de datos Room, DAOs y repositorios
- `ui/auth/` — LoginFragment
- `ui/base/` — BaseActivity, BaseFragment
- `ui/dashboard/` — DashboardFragment, DashboardViewModel
- `ui/transactions/` — TransactionsFragment, AddTransactionFragment, TransactionAdapter
- `ui/categories/` — CategoriesFragment, CategoryAdapter
- `ui/statistics/` — StatisticsFragment
- `utils/` — Utilidades y helpers

## Requisitos
- Android 8.0 (API 26) o superior
- Conexión a internet para login y sincronización

## Autor
Keubis — PDAM UAX 2025/2026

🔗 [Ver repositorio en GitHub](https://github.com/Keubis/Zentryc)