import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

// Lazy loaded views
const LoginView = () => import('@/views/login/LoginView.vue')
const DashboardView = () => import('@/views/dashboard/DashboardView.vue')
const CompaniesView = () => import('@/views/companies/CompaniesView.vue')
const CompanyDetailView = () => import('@/views/companies/CompanyDetailView.vue')
const ContactsView = () => import('@/views/contacts/ContactsView.vue')
const ProjectsView = () => import('@/views/projects/ProjectsView.vue')
const ProjectDetailView = () => import('@/views/projects/ProjectDetailView.vue')
const TendersView = () => import('@/views/tenders/TendersView.vue')
const ContractsView = () => import('@/views/contracts/ContractsView.vue')
const ContractDetailView = () => import('@/views/contracts/ContractDetailView.vue')
const ApplicationsView = () => import('@/views/applications/ApplicationsView.vue')
const SubcontractorsView = () => import('@/views/subcontractors/SubcontractorsView.vue')
const OperativesView = () => import('@/views/operatives/OperativesView.vue')
const PlantView = () => import('@/views/plant/PlantView.vue')
const ProcurementView = () => import('@/views/procurement/ProcurementView.vue')
const HealthSafetyView = () => import('@/views/healthsafety/HealthSafetyView.vue')
const AdoptionView = () => import('@/views/adoption/AdoptionView.vue')
const QualityView = () => import('@/views/quality/QualityView.vue')
const AdminView = () => import('@/views/admin/AdminView.vue')

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: DashboardView,
    meta: { requiresAuth: true, title: 'Dashboard' }
  },
  {
    path: '/companies',
    name: 'Companies',
    component: CompaniesView,
    meta: { requiresAuth: true, title: 'Companies' }
  },
  {
    path: '/companies/:id',
    name: 'CompanyDetail',
    component: CompanyDetailView,
    meta: { requiresAuth: true, title: 'Company Details' }
  },
  {
    path: '/contacts',
    name: 'Contacts',
    component: ContactsView,
    meta: { requiresAuth: true, title: 'Contacts' }
  },
  {
    path: '/projects',
    name: 'Projects',
    component: ProjectsView,
    meta: { requiresAuth: true, title: 'Sites / Projects' }
  },
  {
    path: '/projects/:id',
    name: 'ProjectDetail',
    component: ProjectDetailView,
    meta: { requiresAuth: true, title: 'Project Details' }
  },
  {
    path: '/tenders',
    name: 'Tenders',
    component: TendersView,
    meta: { requiresAuth: true, title: 'Tenders' }
  },
  {
    path: '/contracts',
    name: 'Contracts',
    component: ContractsView,
    meta: { requiresAuth: true, title: 'Contracts' }
  },
  {
    path: '/contracts/:id',
    name: 'ContractDetail',
    component: ContractDetailView,
    meta: { requiresAuth: true, title: 'Contract Details' }
  },
  {
    path: '/applications-for-payment',
    name: 'ApplicationsForPayment',
    component: ApplicationsView,
    meta: { requiresAuth: true, title: 'Applications for Payment' }
  },
  {
    path: '/subcontractors',
    name: 'Subcontractors',
    component: SubcontractorsView,
    meta: { requiresAuth: true, title: 'Subcontractors' }
  },
  {
    path: '/operatives',
    name: 'Operatives',
    component: OperativesView,
    meta: { requiresAuth: true, title: 'Operatives' }
  },
  {
    path: '/plant',
    name: 'Plant',
    component: PlantView,
    meta: { requiresAuth: true, title: 'Plant' }
  },
  {
    path: '/procurement',
    name: 'Procurement',
    component: ProcurementView,
    meta: { requiresAuth: true, title: 'Procurement' }
  },
  {
    path: '/healthsafety',
    name: 'HealthSafety',
    component: HealthSafetyView,
    meta: { requiresAuth: true, title: 'Health & Safety' }
  },
  {
    path: '/adoption',
    name: 'Adoption',
    component: AdoptionView,
    meta: { requiresAuth: true, title: 'Adoption' }
  },
  {
    path: '/quality',
    name: 'Quality',
    component: QualityView,
    meta: { requiresAuth: true, title: 'Quality' }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: AdminView,
    meta: { requiresAuth: true, title: 'Admin', roles: ['admin'] }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guard
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  const requiresAuth = to.meta.requiresAuth !== false
  
  if (requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else if (to.path === '/login' && authStore.isAuthenticated) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
